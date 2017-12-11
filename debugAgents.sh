#!/bin/bash -ex

./mvnw clean install
MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9009" ./mvnw -Pjade-agent exec:java
