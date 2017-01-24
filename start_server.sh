#!/bin/bash

echo "****** BUILDING MODULE ******"
mvn clean install

echo "***** SET ENVIRONMENT ******"
mvn dependency:copy-dependencies
for file in `ls target/dependency`; do export CLASSPATH=$CLASSPATH:target/dependency/$file; done
export CLASSPATH=$CLASSPATH:target/classes

echo "****** RUN SPARK *****"
java -cp $CLASSPATH com.mycompany.app.App
