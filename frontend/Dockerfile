FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    maven \
    fakeroot \
    binutils \
    wget \
    libgtk-3-0 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

RUN jpackage \
    --input target/ \
    --name MiraCloud \
    --main-jar frontend-1.0-SNAPSHOT.jar \
    --main-class org.miracloud.frontend.Launcher \
    --type deb \
    --dest output/