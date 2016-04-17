FROM java:jre-alpine

RUN apk update \
      && apk upgrade \
      && apk add --no-cache \
        less \
        tar \
        bash \
      && rm -rf /var/cache/apk/*

COPY build/distributions/theta.tar theta.sh shutdown.sh /opt/

RUN tar -xvf /opt/theta.tar --directory /opt/ \
      && rm /opt/theta.tar \
      && mv /opt/theta.sh /opt/shutdown.sh /opt/theta/ \
      && adduser -D -s /bin/bash theta \
      && chgrp theta /opt/theta \
      && chmod 775 /opt/theta

USER theta
WORKDIR /opt/theta/

CMD ["/opt/theta/theta.sh"]
