FROM kong:0.11.2
LABEL maintainer="micah.silverman@okta.com, andy.march@okta.com"

RUN yum install -y git unzip openssl openssl-devel gcc lua-devel luarocks && yum clean all
RUN luarocks install kong-oidc

COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 8000 8443 8001 8444

STOPSIGNAL SIGTERM

CMD ["kong", "docker-start"]