## Okta Origin Example

This example uses a Spring Boot app with Spring Security to tie authentication and authorization to an incoming HTTP
header.

The expectation is that this app is configured to only accept connections from an API Gateway and that the API
Gateway sets the expected header once the user has authenticated.

This example uses the [Kong](https://getkong.org/) API Gateway connected to [Okta](https://okta.com) 
using [OIDC](http://openid.net/specs/openid-connect-core-1_0.html) via the 
[Kong OIDC Plugin](https://github.com/nokia/kong-oidc).

To setup this example on your own, you'll need to do the following:

1. Create an Okta Developer Account
2. Create some users and groups
3. Setup an OIDC Application on Okta
4. Setup additional claims for the default Authorization Server on Okta
4. Deploy this application to [Docker](https://www.docker.com/), which includes the Kong API Gateway and this 
Spring Boot application
5. Configure the Kong API Gateway to connect to the Spring Boot app
6. Configure the Kong API Gateway to authenticate to Okta

### Setup Okta

Head over to [https://developer.okta.com](https://developer.okta.com) and create a free Okta Developer Account.

Finish setting up your account by following the link you receive in your email.

From your Admin Console Dashboard, click `Users` -> `Groups`. Click `Add Group`. Enter `users` in the `Name` field
and click `Add Group`. Repeat adding a group, only this time, name the group `admins`.

![groups](/images/groups-1.png)

Click `Users` -> `People`. Click `Add Person`.

Fill out the form and put `users` in the `Groups` field. It's important that either the `Primary email` or 
`Secondary email` be a real email address.

Make sure `Send user activation email now` is checked. Click `Save and Add Another`

![user](/images/users-1.png)

Repeat the process, only this time, put `users` and `admins` in the `Groups` field.

![user](/images/users-2.png)

You should receive a confirmation email for each of the users. Make sure you follow the link in each email to finish
setting up these users.

From your Admin Console Dashboard, click `Applications`. Then, click `Add Application`.

Click `Web` and click `Next`.

Fill out the form like so:

![user](/images/applications-1.png)

The important things here are:

* Enter `http://localhost:8000` and `http://localhost:8000/cb` as `Login redirect URIs`
* Enter `users` and `groups` in the `Group assignments` field
* Make sure `Implicit` is checked in the `Grant type allowed` section

Click `Done`

On the `General` tab, scroll down and make note of the `Client ID` and the `Client secret`. You will need them later
when you configure the Kong oidc plugin.

![user](/images/applications-2.png)

Click `API` -> `Authorization Servers`. Click `default`

Click `Claims`

![as](/images/as-1.png)

Click the pencil to the right of the `Groups` claim. This is the edit button. Select `ID Token` from the 
`Include in token type` dropdown and click `Save`.

![as](/images/as-2.png)

Click `Add Claim`. Fill in the form as follows:

![as](/images/as-3.png)

Click `Create`

Click `Add Claim`. Fill in the form as follows:

![as](/images/as-4.png)

Click `Create`

You should now see the Claims tab like so:

![as](/images/as-5.png)


The Kong oidc plugin creates an `X-Userinfo` header based on the information found in the ID Token. The Spring Boot
app looks for this header and builds the granted authorities list from the list of `groups`. It also expects a
`user.fullName` and `user.email` to be in the header.


### Setup Docker

Setup Docker on your local machine by clicking `Get Docker` on [https://www.docker.com/](https://www.docker.com/).

All the screenshots below are on Mac.

There are two Docker images associated with this project. One is for the Kong API Gateway with the OIDC plugin. The
other is the Spring Boot app that Kong will proxy to once the user has authenticated.

We'll build the images and then run them in Docker containers.

First, the Kong API Gateway:

```
cd docker/okta-kong-oidc
docker build -t okta-kong-oidc .
```

Then, the Spring Boot app:

```
cd ../header-origin-example
docker build -t header-origin-example .
```

![user](/images/docker-1.png)

Grab a cup of coffee...

Next, we'll create a Docker network for all our containers to use:

```
docker network create okta-kong-bridge
```

Now, we'll create the containers:

```
docker run -d --name kong-database \
    --net okta-kong-bridge \
    cassandra:3
```

This sets up a [Cassandra](http://cassandra.apache.org/) database for Kong to use.

```
docker run --rm \
    --net okta-kong-bridge \
    -e "KONG_DATABASE=cassandra" \
    -e "KONG_CASSANDRA_CONTACT_POINTS=kong-database" \
    okta-kong-oidc:latest kong migrations up
```

This prepares the cassandra database with the latest migrations for use with Kong.

```
docker run -d --name okta-kong-oidc \
    --net okta-kong-bridge \
    -e "KONG_LOG_LEVEL=debug" \
    -e "KONG_CUSTOM_PLUGINS=oidc" \
    -e "KONG_LUA_SSL_TRUSTED_CERTIFICATE=/etc/ssl/certs/ca-bundle.crt" \
    -e "KONG_LUA_SSL_VERIFY_DEPTH=2" \
    -e "KONG_DATABASE=cassandra" \
    -e "KONG_CASSANDRA_CONTACT_POINTS=kong-database" \
    -e "KONG_PROXY_ACCESS_LOG=/dev/stdout" \
    -e "KONG_ADMIN_ACCESS_LOG=/dev/stdout" \
    -e "KONG_PROXY_ERROR_LOG=/dev/stderr" \
    -e "KONG_ADMIN_ERROR_LOG=/dev/stderr" \
    -p 8000:8000 \
    -p 8443:8443 \
    -p 8001:8001 \
    -p 8444:8444 \
    okta-kong-oidc:latest
```

This creates a container using the `okta-kong-oidc` image we created above. 
Let's look a little more closely at what's going on here.

* On the second line, we reference the network we created. This allows this container to connect to other container on 
the same network.
* On the fourth line, we set and environment variable to tell Kong to use the oidc plugin
* Lines five and six ensure that Kong can reference the root certificate authority for SSL connections. This is
important for being able to connect securely to Okta.
* The last line references the image we created above

```
docker run -d --name header-origin-example \
    --net okta-kong-bridge \
    header-origin-example:latest
```

This creates a container using the `header-origin-example` image we created above.

Notice that we are not exposing any ports to the host. This ensures that *only* containers on the `okta-kong-bridge`
can connect to the Spring Boot app. This is important in gateway-backed applications. You don't want someone
connecting directly to the application that sites behind the Gateway. 

At this point, there should be three Docker containers running: `kong-database`, `okta-kong-oidc`, and 
`header-origin-example`.

We can confirm this by running:

```
docker container ls
```

You should see something like this:

```
CONTAINER ID        IMAGE                          COMMAND                  CREATED             STATUS              PORTS                                                                NAMES
5fd2c1715554        header-origin-example:latest   "/docker-entrypoin..."   14 minutes ago      Up 14 minutes                                                                            header-origin-example
1eea39a21ec7        okta-kong-oidc:latest          "/docker-entrypoin..."   14 minutes ago      Up 14 minutes       0.0.0.0:8000-8001->8000-8001/tcp, 0.0.0.0:8443-8444->8443-8444/tcp   okta-kong-oidc
bf8b41319903        cassandra:3                    "/docker-entrypoin..."   14 minutes ago      Up 14 minutes       7000-7001/tcp, 7199/tcp, 9042/tcp, 9160/tcp                          kong-database
```

### Configure Kong

Now that our docker container is running, we need to wire up the Kong API Gateway to our Spring Boot application and to
Okta for authentication.

The examples below use [HTTPie](https://httpie.org) - a modern curl replacement

```
http -f POST localhost:8001/apis/ \
    name=okta-secure \
    upstream_url=http://header-origin-example:8080 \
    uris=/
```

This command uses Kong's Admin API, which runs on port `8001` by default. Notice how the `upstream_url` is connecting
to the Spring Boot app which runs on port `8080` (within its container). The Docker networking allows us to reference
the name of one container from another - as long as they're all on the same network.

```
http POST localhost:8001/apis/okta-secure/plugins name=oidc \
config.client_id="<Okta OIDC App Client ID>" \
config.client_secret="<Okta OIDC App Client Secret>" \
config.discovery="https://<Okta Tenant Name>/oauth2/default/.well-known/openid-configuration"
``` 

This command configures the Kong oidc plugin to connect to the Okta oidc application you setup earlier.

### Action!

If all has gone well to this point, you have a Docker container running the Kong API Gateway and this Spring Boot
application. Further, Kong is configured with the oidc plugin connected to Okta and to proxt requests to the Spring
Boot app once you've authenticated. The Spring Boot app is *not* accessible directly from your host machine, but only
from within the Docker container by Kong.

Browse to: `http://localhost:8000`

You should immediately be redirected to the Okta login screen.

![action](/images/action-1.png)

Log in as the user you created that belongs to the `users` group.

![action](/images/action-2.png)

Click `Users Only`

![action](/images/action-3.png)

You should see a screen showing the groups you belong to.

Click `Back` and then click `Admins Only`

![action](/images/action-4.png)

You should see a `403` Unauthorized message, since this user does not belong to the `admins` group.

Now, login as the admin user you setup before.

![action](/images/action-5.png)

This time, when you click on `Admins Only`, you should see the page since this user belongs to the `admins` group.

![action](/images/action-6.png)