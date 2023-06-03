#!/bin/bash

echo "Performing a clean Maven build"
mvn clean package -DskipTests=true

echo "Setting the default builder for pack"
pack config default-builder paketobuildpacks/builder:tiny

echo "Packing the proxy layer"
cd proxy-layer
pack build smartdoc-proxy-service --env "BP_JVM_VERSION=11.*"
cd ..

echo "Packing the Eureka Discovery Server"
cd proxy-registry
pack build smartdoc-proxy-registry --env "BP_JVM_VERSION=11.*"
cd ..

echo "Packing the Spring Cloud Gateway"
cd gateway-proxy-layer
pack build smartdoc-proxy-gateway --env "BP_JVM_VERSION=11.*"
cd ..
