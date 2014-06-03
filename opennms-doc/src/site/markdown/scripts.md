## Script Steps
To provide easy customization options, scripts can be applied to the result of the mapper before the final requisition is delivered.
Every script step can manipulate the requisition and has access to the raw result of the source. Additionally access to the configuration, logging and some helpers is availible.
To use a script step add a "script" property to the requisition.properties file and reference the script file you want to run.
Multiple scripts can be executed by adding multiple comma separated references to script files to the script property. 
The script will be executed after the mapper has been applied. 
The script will be executed by Apache-BSF.
By default Groovy 2.2.2 and Beanshell 2.0b5 are supported. 

Every script step is provided with the following parameter:

* A Path object called script that points the the script file
* A Object called data, that contains a Computers or SnmpDevices object, depending on the source that is used.
* A Requisition object called requisition with the mappings result from the mapper.
* A Logger object called logger from the slf4j project.
* A Configuration object called config that contains all configured parameters, from the apache.commons.configuration framework.
* An instance of the IpInterfaceHelper called ipInterfaceHelper, that provides the black- and whitelisting.

Every script step has the provide a Requisition object as its result. For every request of a requisition each script step is reloaded. 
As a reference the folder "src/examples/" contains examples. 