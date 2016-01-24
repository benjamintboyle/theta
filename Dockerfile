FROM java:jre
COPY ./build/libs/theta*.jar /opt/theta
WORKDIR /opt/theta
CMD ["java", "ThetaEngine"]
