FROM java:jre

EXPOSE 2001

COPY libs/IBJts/ /opt/
WORKDIR /opt/IBJts
CMD ["java", "-cp", "jts.jar:total.2013.jar", "-Dsun.java2d.noddraw=true", "-Xmx512M", "ibgateway.GWClient"]

COPY build/libs/theta*.jar /opt/theta/bin/
CMD ["java", "-jar", "/opt/theta/bin/theta-0.1.jar"]
