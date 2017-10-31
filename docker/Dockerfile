FROM centos:7
MAINTAINER Micah Silverman, micah.silverman@okta.com

ENV KONG_VERSION 0.11.1

RUN yum install -y wget https://bintray.com/kong/kong-community-edition-rpm/download_file?file_path=dists%2Fkong-community-edition-$KONG_VERSION.el7.noarch.rpm
RUN yum install -y which git unzip java-1.8.0-openjdk-devel.i686 && yum clean all
RUN luarocks install kong-oidc
RUN git clone https://github.com/dogeared/okta-origin-example
RUN cd okta-origin-example && ./mvnw clean install

COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 8000 8443 8001 8444

STOPSIGNAL SIGTERM

CMD ["/usr/local/openresty/nginx/sbin/nginx", "-c", "/usr/local/kong/nginx.conf", "-p", "/usr/local/kong/"]