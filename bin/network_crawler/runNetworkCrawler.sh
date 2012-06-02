#!/bin/sh

for jar in ../../lib/*.jar;do
    CLASSPATH=$CLASSPATH:$jar
done

MEM_ARGS="-Xmx2G"
CLASS_TO_RUN=twitter4j.examples.pyongjoo.networkCrawler.NetworkCrawler
OUTPUTFILE="/home/pyongjoo/workspace/tweetsprocess/data/network-crawl/network.csv"
SEEDFILE="seed.txt"

java $MEM_ARGS -cp $CLASSPATH $CLASS_TO_RUN $OUTPUTFILE $SEEDFILE \
    1> stdout.log 2> stderr.log
