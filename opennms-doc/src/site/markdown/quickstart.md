## Quickstart example
To give an example we want to provide two requisitions from an poor mans inventory as _XLS file_ (myInventory.xls).  The first requisition has an worksheet containing all router and the second worksheet has all server of our network. This example can be found in `src/examples/xls.source`.

![Worksheet with Router](images/myRouter.png "Worksheet with Router")

In line 5, 6 and 7 there is an Router defined with more than one IP interface. All three interfaces will be manually provisioned. The private IP interface with _192.168.30.1_ is not used for SNMP agent communication. The services ICMP, SNMP and StrafePing are forced on some IP interfaces. For all other IP interfaces you can use the OpenNMS Provisiond mechanism scanning IP interface table from SNMP and the detectors for additional services. The server will also be categorized in _Backbone_ and _Office_.

![Worksheet with Server](images/myServer.png "Worksheet with Server")

The _OpenNMS_ requisition should be provided via _HTTP_ and we use _OpenNMS Provisiond_ to synchronize it on a regular base. We build the following file structure:

    [root@localhost opennms-pris]# clear && pwd && tree
    /opt/opennms-pris
    .
    ├── global.properties
    ├── myInventory.xls
    ├── myRouter
    │   └── requisition.properties
    ├── myServer
    │   └── requisition.properties
    └── opennms-pris.jar

Providing the _OpenNMS requisition_ over _HTTP_ we create the following `global.properties`

__File: global.properties__

    ## start an http server that provides access to requisition files
    driver = http
    host = 127.0.0.1
    port = 8000

    ### file run to create a requisition file in target
    #driver = file
    #target = /tmp/

The HTTP server is listening on localhost port 8000/TCP. We have to create two directories, each directory `myServer` and `myRouter` have a `requisition.properties` file. Both `requisition.properties` reference the main `myInventory.xls` file which contains two worksheets named _myServer_ and _myRouter_. The `requisition.properties` is for both requisitions the same. It is possible to create for each requisition different script or mapping steps.

__File: requisition.properties__

    source = xls.source
    mapper = echo.mapper
    xls.file = ../myInventory.xls

It is not necessary to restart the _pris_ server if you change property files or the _XLS_ file. All changes will be executed with the next request against the server. With the given configuration you see the result of the OpenNMS requisitions with the URL http://localhost:8000/myRouter and http://localhost:8000/myServer and can be used in _OpenNMS Provisiond_.

__provisiond-configuration.xml__

    <requisition-def import-name="myRouter" import-url-resource="http://localhost:8000/myRouter">
        <cron-schedule>0 0 0 * * ? *</cron-schedule>
    </requisition-def>

    <requisition-def import-name="myServer" import-url-resource="http://localhost:8000/myServer">
        <cron-schedule>0 0 1 * * ? *</cron-schedule>
    </requisition-def>


![Pris output for OpenNMS Provisiond via HTTP](images/requisitions-http.png "Pris output for OpenNMS Provisiond via HTTP")

