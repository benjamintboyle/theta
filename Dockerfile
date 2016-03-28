FROM java:jre

EXPOSE 2001

#WORKDIR /opt/ibgateway/
#COPY libs/IBJts/ .
#CMD ["java", "-cp", "jts.jar:total.2013.jar", "-Dsun.java2d.noddraw=true", "-Xmx512M", "ibgateway.GWClient"]

WORKDIR /opt/theta/
COPY build/libs/theta*.jar bin/
# CMD ["java", "-jar", "/opt/theta/bin/theta-0.1.jar"]
