#! /bin/sh
# 
# version 0.4 for Redhat RPM 
#
# 0.1 - clean up original shell since env is known
# 0.2 - set ANT_HOME to /usr/share/ant
# 0.3 - follow Debian java policy and install jar libs in /usr/share/java
# 0.4 - ant 1.3, added jikes stuff, optional.jar and in case of JDK 1.2, rt.jar
#

ANT_HOME=/usr/share/ant

if [ -f $HOME/.antrc ] ; then 
  . $HOME/.antrc
fi

# Allow .antrc to specifiy flags to java cmd
if [ "$JAVACMD" = "" ] ; then 
  JAVACMD=java
fi

LOCALCLASSPATH=/usr/share/java/ant.jar:/usr/share/java/jaxp.jar:/usr/share/java/optional.jar:/usr/share/java/parser.jar

if [ "$CLASSPATH" != "" ] ; then
  LOCALCLASSPATH=$CLASSPATH:$LOCALCLASSPATH
fi

if [ "$JAVA_HOME" != "" ] ; then
  if test -f $JAVA_HOME/lib/tools.jar ; then
    LOCALCLASSPATH=$LOCALCLASSPATH:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar
  fi
  if test -f $JAVA_HOME/jre/lib/rt.jar ; then
	LOCALCLASSPATH=$LOCALCLASSPATH:$JAVA_HOME/jre/lib/rt.jar
  fi
  if test -f $JAVA_HOME/lib/classes.zip ; then
    LOCALCLASSPATH=$LOCALCLASSPATH:$JAVA_HOME/lib/classes.zip
  fi
fi

# supply JIKESPATH to Ant as jikes.class.path
if [ "$JIKESPATH" != "" ] ; then
  if [ "$ANT_OPTS" != "" ] ; then
    ANT_OPTS="$ANT_OPTS -Djikes.class.path=$JIKESPATH"
  else
    ANT_OPTS=-Djikes.class.path=$JIKESPATH
  fi
fi

$JAVACMD -classpath $LOCALCLASSPATH -Dant.home=${ANT_HOME} $ANT_OPTS org.apache.tools.ant.Main $@

