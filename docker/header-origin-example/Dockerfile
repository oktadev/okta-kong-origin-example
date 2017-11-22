FROM centos:7
MAINTAINER Micah Silverman, micah.silverman@okta.com

ENV KONG_VERSION 0.11.1

RUN yum install -y which git java-1.8.0-openjdk-devel.i686 && yum clean all
RUN git clone https://github.com/oktadeveloper/okta-kong-origin-example
RUN cd okta-kong-origin-example && ./mvnw clean install

COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]

STOPSIGNAL SIGTERM

CMD cd okta-kong-origin-example && ./mvnw spring-boot:run

