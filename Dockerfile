FROM openjdk:9-jre-slim

RUN ["useradd", "-p", "theta", "theta"]

COPY build/distributions/theta.tar /opt/

RUN tar -xvf /opt/theta.tar --directory /opt/ \
      && rm /opt/theta.tar \
      && chgrp theta /opt/theta \
      && chmod 775 /opt/theta

USER theta
WORKDIR /opt/theta/

CMD ["/opt/theta/bin/theta"]
