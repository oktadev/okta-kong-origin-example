FROM kong:2.3.0-alpine

MAINTAINER Micah Silverman, micah.silverman@okta.com

USER root

RUN apk add httpie jq
RUN luarocks install kong-oidc

COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 8000 8443 8001 8444

STOPSIGNAL SIGTERM

CMD ["/usr/local/openresty/nginx/sbin/nginx", "-c", "/usr/local/kong/nginx.conf", "-p", "/usr/local/kong/"]
