FROM openjdk:10-jre-slim

RUN ["useradd", "-p", "theta", "theta"]

COPY build/distributions/theta.tar /opt/

RUN tar -xvf /opt/theta.tar --directory /opt/ \
      && rm /opt/theta.tar \
      && mkdir /opt/theta/logs \
      && chgrp theta /opt/theta /opt/theta/logs \
      && chmod 775 /opt/theta /opt/theta/logs

USER theta
WORKDIR /opt/theta/

CMD ["/opt/theta/bin/theta"]
