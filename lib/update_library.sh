#!/bin/sh

rm ./*.jar

cp ../twitter4j-async/target/*.jar ./
cp ../twitter4j-core/target/*.jar ./
cp ../twitter4j-examples/target/*.jar ./
cp ../twitter4j-media-support/target/*.jar ./
cp ../twitter4j-stream/target/*.jar ./

