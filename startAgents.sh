#!/bin/bash -ex

./mvnw clean install
./mvnw -Pjade-agent exec:java
