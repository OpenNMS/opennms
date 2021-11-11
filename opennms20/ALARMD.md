
# Alarmd
general concept nodes:
should depend on eventd -- features sentinel-alarm pulls in sentinel-eventd
definition of done:
1. same event-stress events?  alert-stress?
check to see results in inserts into alarm table
write alarm-list command
2. check drools functionality 
write a test suite of events to do a trigger clear
to trip drools rule and test

( next steps : REST API post event via rest API, query alarms via rest API)
( next next steps:  distributed, IPC,  camel)

1) in karaf container load this bundle
drools features non-existent
   need to write
   drools OSGI compatible
   
3) iteratively satisfy dependencies for that bundle

a) use application context config, translate to blueprint
b) initializing beans require the after-prop attribute on bean
c) grep existing feature files in (container/feature)

4) collect all bundles/feature dependencies into a feature file

5) we get interface from container 
6) iteratively add blueprint wiring for that service
src/main/resources/META-INF/opennms/applicationContext-alarmd.xml
a) code greping in app files for associted beans
b) but persistence level already provided, no wiring at-below persistence
c) sessionUtils replaces transactionManager (refactor assoc. tests as well)

7) leverage/write shell commands to exercise new feature


