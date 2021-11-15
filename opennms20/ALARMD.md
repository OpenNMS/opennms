# Alarmd

should depend on eventd -- features sentinel-alarm pulls in sentinel-eventd

* src/main/resources/META-INF/opennms/applicationContext-alarmd.xml
* src/main/resources/OSGI-INF/blueprint/blueprint.xml
(start empty, or just containing the exported service)

* in karaf container load this bundle (drools OSGI compatible?)
   - iteratively satisfy dependencies for that bundle    
   - grep existing feature files in (container/feature)
  - collect all bundles/feature dependencies into a feature file

* Wire from applicationContext config, translating to blueprint
   - code grep-ing in app files for associated beans
   - no wiring below persistence level already provided
   - subclass of _InitializingBean_ requires the after-prop attribute on bean
   ` ... init-method="afterPropertiesSet">`
   - pair these up with detroy methods in case they are missing  
   - onsgi:list -> BP reference List
   - if possible: annotation _EventHandler_ filter in BP
    `@EventHandler(uei = EventHandler.ALL_UEIS)
    public void onEvent(IEvent e) {`
   - _@Autowired_ vars require explicit setters and for BP bean injection
   - use the non-sprint _transactions_ with SessionUtils
      replace @Transactional with a wrap of 
      `sessionUtils.withTransaction(() -> { ... });`
      replace TransactionOperations with a wrap of
      `sessionUtils.withTransaction(() -> { ... });` (clean import, refactor test)
   
* Leverage/write shell commands to exercise new feature

* Definition of Done:
   - same event-stress events?  alert-stress?
check to see results in inserts into alarm table
write alarm-list command
   -check drools functionality
write a test suite of events to do a trigger clear
to trip drools rule and test

* ( next steps )
  - REST-API post event via rest API, query alarms via rest API)
  - write IT for a dockerized karaf with injected config )
* ( next-next steps:  distributed, IPC,  camel)
 