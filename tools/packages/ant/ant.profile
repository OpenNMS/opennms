#!/bin/bash

JAVADIR=/usr/share/java

for jar in ant optional jaxp parser; do
  if [  x`echo $CLASSPATH | grep "/$jar.jar"` = "x" ]; then
    if [ -e $JAVADIR/$jar.jar ]; then
      export CLASSPATH=$CLASSPATH:$JAVADIR/$jar.jar
    fi
  fi
done
