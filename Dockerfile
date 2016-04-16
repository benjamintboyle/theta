FROM java:jre

EXPOSE 2001

COPY build/distributions/theta.tar theta.sh shutdown.sh /opt/

RUN tar -xvf /opt/theta.tar --directory /opt/; \
      rm /opt/theta.tar; \
      mv /opt/theta.sh /opt/shutdown.sh /opt/theta/; \
      adduser --disabled-password --gecos "" theta; \
      chgrp theta /opt/theta; \
      chmod 775 /opt/theta

USER theta

# CMD ["/opt/theta/theta.sh"]
