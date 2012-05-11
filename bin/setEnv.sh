#!/bin/sh
if  [ -z $JAVA_HOME ] ; then
 export JAVA_HOME="/usr"
fi

for jar in ../lib/*.jar;do
 export CLASSPATH=$CLASSPATH:$jar
done

for jar in ../twitter4j-examples/target/*.jar;
do
 export CLASSPATH=$CLASSPATH:$jar
done

export MEM_ARGS="-Xms30m -Xmx30m"
