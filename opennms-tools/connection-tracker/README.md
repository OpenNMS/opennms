Using the Connection Tracker
============================

TO use this aspect we need to do the following:

1. Copy the connection-tracker jar file to $OPENNMS_HOME/lib
2. Delete $OPENNMS_HOME/lib/spring-aspects*.jar (it gets in the way of our aspectj stuff, and
   is unused in OpenNMS)
3. Add -javaagent:$HOME/.m2/repository/org/aspectj/aspectjweaver/1.6.8/aspectjweaver-1.6.8.jar
   to the java command line (you can add it to ADDITIONAL_MANAGER_OPTIONS in the
   $OPENNMS_HOME/etc/opennms.conf file)
4. Optionally, add -Daj.weaving.verbose=true for more debug info.
5. Optionally, add -Dorg.opennms.debug.showTrackAndComplete=true to show when a connection
   is added or removed from the tracker.

NOTE: you must use the jar because META-INF/aop.xml is in the jar and configures the aspect.