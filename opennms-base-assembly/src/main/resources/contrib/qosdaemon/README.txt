OpenNMS OSS/J Qos Interface
---------------------------
This folder contains information on using the qosdaemon which implements a partially compliant OSS/J interface for OpenNMS. 

Folder: qos_example_configuration : contains example configuration files for the interface and instructions on use

Folder: testscripts : contains scripts to allow you to connect to the OSS/J interface and see the messages

Introduction
------------
This project provides partial implementation of the OSS/J QoS interface for OpenNMS.  It is offered as an illustrative and training tool to explain OSS/J and to gage interest from the OpenNMS community in taking the project forwards. 

This project leverages the OSSbeans project which provides the core classes for the OSS/J implementation. OSSbeans are a seperate project hosted as part of the University of Southampton OpenOSS initiative at http://sourceforge.net/projects/openoss

OSS/J Conformance
-----------------
The interface is based upon the OSS/J QoS specification available at www.ossj.org. The basic principles and design patterns of the the specification are implemented however not all of the mandatory functionality is complete and the interface has not been tested against the OSS/J SDK. 

Where functionality is provided it does so using classes implementing interfaces conforming to the javax.oss interface tree and the XML messaging uses messages conforming to the OSS/J Qos XSD's. This provides a firm basis for moving towards full OSS/J compliance in future releases.

Production Use
--------------
The interface is presently experimental and is not optimised for high load environments.  Although included with OpenNMS, it can be completely disabled and will not then interfere with other OpenNMS components. However even in it's present form the interface may still be useful for some production solutions. Envisaged uses include;

* Integration of OpenNMS alarms with other Operational Support Systems using J2EE or JMS
* Monitoring important alarms from remote OpenNMS systems - potentially on a customer's site. ( Note that to circumnavigate firewalls JbossMQ can be configured to send JMS messages using HTTP - although this has not been tested.)

as an example, the interface has been successfully used to integrate OpenNMS with an Alarm / Topology  correllation engine from Sidonis. www.sidonis.com as part of a proof of concept for managing a Digital TV network

Liscence
----------
The qosdaemon project builds an OSS/J interface for OpenNMS. It is released with OpenNMS under the AGPL licence and uses code contributed to OpenNMS by the University of Southampton.

OSSbeans ( http://sourceforge.net/projects/openoss ) are released under the Apache-2 licence by the University of Southampton

Functionality Provided
----------------------
The current release of the qosdaemon module leverages OSSbeans Release 2.1.0 and provides the following functionality. The module provides two daemons which can be used independently or together. The qosd daemon publishes the internal OpenNMS alarm list as an OSS/J alarm list. The qosdrx daemon allows an OpenNMS system to connect using the OSS/J interface to remote OpenNMS systems running qosd. This allows a 'master' OpenNMS to monitor the state of the alarms lists in 'slave' openNMS systems. The present implementation is almost exclusively JMS event driven with limited alarm list query functionality provided as a J2EE option on qosd. 

The implementation leverages JbossMQ as the JMS provider. In theory other JMS providers could be used but these have not been tested.

Qosd
----
The Qosd daemon monitors the OpenNMS alarm list and generates OSS/J JMS events corresponding to changes in the state of the alarms in the list. It can run in two modes; natively on OpenNMS or in conjunction with a separate J2EE application. 

a. Native OpenNMS provided interface.
OpenNMS does not run natively in a J2EE container but leverages the spring framework and JMX to provide a container like environment for it's daemons. The qosd daemon code can run natively as a spring application within OpenNMS. In this case it uses the OSS/J XVT ( XML over JMS ) profile to publish alarm list changes . The qosd daemon publishes OSS/J AlarmEvents as both JMS TextMessages and as JMS ObjectMessages containing AlarmEvent objects.

b. Separate J2EE server provided interface.
An alternative configuration is possible where the qosd daemon connects to an ejb application running in a seperate J2EE server. This application is known as OSSBeans-qos-ear and is available from the OSSbeans site. In this mode the ejb exposes OSS/J semantics and allows external applications to connect with the ejb as an OSS/J JVT interface. Note that only a very limited alarm list query functionality is provided ( query for all alarms ). 
Note that this configuration requires a J2EE server (Jboss) to be hosting a OSSBeans-qos-ear locally to each OpenNMS implementation which is running qosd in this mode. In most circumstances, it is easier to use the native interface for the remote machines they can all use a single JbossMQ deployment and a local J2EE server is not required for each OpenNMS.
Which mode qosd is running is determined by a setting in the opennms.conf file.
To use the native OpenNMS provided interface use -Dqosd.usej2ee=false 
To use the seperate J2EE server provided interface use -Dqosd.usej2ee=true

Qosdrx
------
The qosdrx daemon can connect to multiple OSS/J event topics hosted on a JbossMQ server and receive OSS/J alarm events from remote OpenNMS systems running qosd. The local alarm list will be updated to reflect the remote alarm lists. Note that no resynchronization capability is provided at this time so it is possible for the alarm lists to get out of alignment if messages are lost. However in practice, the JMS messaging system should provide a reliable transport.

