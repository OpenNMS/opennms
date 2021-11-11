
# Eventd

1) create blueprint only exposing interface for eventd
   src/main/resources/OSGI-INF/blueprint/blueprint.xml
2) in karaf container load this bundle
3) iteratively satisfy dependencies for that bundle

a) use application context config, translate to blueprint
b) initializing beans require the after-prop attribute on bean
c) grep existing feature files in (container/feature)

4) collect all bundles/feature dependencies into a feature file

5) we get interface from container 
6) iteratively add blueprint wiring for that service

a) code greping in app files for associted beans
b) but persistence level already provided, no wiring at-below persistence
c) sessionUtils replaces transactionManager (refactor assoc. tests as well)

7) leverage/write shell commands to exercise new feature


