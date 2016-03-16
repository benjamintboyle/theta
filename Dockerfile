FROM java:jre
COPY libs/IBJts/ /opt/
WORKDIR /opt/IBJts
CMD ["java", "-cp", "jts.jar:total.2013.jar", "-Dsun.java2d.noddraw=true", "-Xmx512M", "ibgateway.GWClient"]

WORKDIR /opt/theta/
COPY build/libs/theta*.jar /opt/theta/
CMD ["java", "ThetaEngine"]
