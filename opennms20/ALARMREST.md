# Alarmd

  - REST-API post event via rest API, query alarms via rest API)
    
* copy over endpoints from v2 DONE
* solve jaxb problem DONE
* get the sessionutils to force initizlize the hydrated objects before marshalling. solve using set(get) using DTO mapper
  and decouple the Hibernate entities from the objects that are used in the REST API as is already done in /api/v2/alarms endpoint: 
  https://github.com/OpenNMS/opennms/blob/opennms-29.0.1-1/opennms-webapp-rest/src/main/java/org/opennms/web/rest/v2/AlarmRestService.java#L342
* wire in ticketer when this service is done
* authentication and authorization in karaf using user.props DONE
* wire in the Opennms jaas-login-module (Blueprint) 

  - write IT for a dockerized karaf with injected config 
* ( next-next steps:  distributed, IPC,  camel)
 