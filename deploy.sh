#!/usr/bin/env bash

TARGET_SERVER_DIRECTORY="/home/evan/Jahia/Jahia-EE-8.0.1.0-SDK/digital-factory-data/"

curl -sS --user root:Hayward1234 \
   --form bundle="@target/product-catalog-hayward-1.0.0-SNAPSHOT.jar" \
   --form start=true https://hayward-cnisenbaum.cloud.jahia.com/modules/api/bundles

#cp -uvv "${PWD}/src/main/resources/META-INF/xml/products.xml" -t "${TARGET_SERVER_DIRECTORY}"

#for MODULE in "$@"
#do
#  curl -s --user root:root --form bundle="@target/$MODULE-2.0.0.jar" \
#    --form start=true http://localhost:8080/modules/api/bundles
#done

#/opt/apache-maven-3.6.3/bin/mvn clean install jahia:deploy -s /opt/apache-maven-3.6.3/conf/settings.xml -f /data/digital-factory-data/sources/viagem-web-components -P localhost
