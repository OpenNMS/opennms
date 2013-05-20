                                                                        MCG PM Setup Details:                                  
                      

1.	The examples directory (/opt/opennms/etc/examples) is delivered as part of OpenNMS to refer to sample configs and see how to use them in their own environments.  

2.	For MCG PM Config, the following are the files to be configured. 
      
    	/opt/opennms/etc/collectd-configuration.xml 
-	Copy the MCG_collectd-configuration.xml available under /opt/opennms/etc/examples/mcg  directory. rename and edit it. 
-	Add the IP addresses of the devices to collect data under the desired packages 
	
	There are 2 bundles packages which can be used for collection.
-	3GPP-Full-15min - Collects 3GPP data/stats which are collected at 15 minutes interval in 3GPP XML format by MCG device.
-	3GPP-Full-30min -Collects 3GPP data/stats which are collected at 30 minutes interval in 3GPP XML format by MCG device.
-	
               -  Service Name should be XMLCollection and following class to be configured for   
                XML collection.

                <collector service="XMLCollection" class-       
                    name="org.opennms.protocols.xml.collector.XmlCollector"/>

              - Change the service status of the XML collection to on for the selected package.    
                By default, it is turned on for 3GPP-Full-5min. 
       <service name="XMLCollection" interval="300000" user-defined="false"     
                status="on"> 
	- Save the changes into openms/etc/collectd-configuration.xml 
	 
                     
3.	opennms/etc/xml-datacollection-config.xml 

-	Copy the MCG_ xml-datacollection-config.xml available under /opt/opennms/etc/examples/mcg  directory. rename and edit it.

  The XML files shall be retrieved by using SFTP protocol.

-	Edit the following for each package in xml-datacollection-config.xml.

             <xml-source                  
             url="sftp.3gpp://opennms:Op3nNMS@{ipaddr}/opt/hitachi/cnp/data/pm/reports/3gpp/5?
              step={step}&amp;timezone={timezoneid};&amp;neId={foreignId}&amp;deleteFile=true">
     
              -    {username}, {password} and {ipaddr} : user name, password and Ip address of        
                    MCG device.
 
-	{step}: value of step size in each package 
-	 {foreignId}: value should be NE Id of the device.
-	{timezoneid}: valid time zone Id
Example:

<xml-source url="sftp.3gpp://mtc:mtc@10.213.2.217/opt/hitachi/cnp/data/pm/reports/3gpp/5?step=300&amp;neId=MCG00023&amp;timezone=Etc/UTC&amp;deleteFile=true">

NOTE : Please ensure the time stamp of opennms machine and the device(MCG) should be same because of the file format.

                This is a simplified way to import a set of performance metrics that could be 
                Shared across many sources from different XML collections
                 
               <import-groups>xml-datacollection/3gpp.full.xml</import-groups>
       
4.	 3gpp.full.xml/3gpp.kpis.xml file should be in place in 
	-/opt/opennms/etc/xml-datacollection/3gpp.full.xml  or  3gpp.kpis.xml
            This has the details of the stats to be collected for the respective packages 
            described above. 
            Copy the file 3gpp.kpis.xml available under /opt/opennms/etc/examples/mcg      
            to above path if you want to see only kpis to plot graphs.     
        
     5.     The resource label definitions for the groups/tables are in
            -/opt/opennms/etc/datacollection/3gpp.xml 
             Copy the file 3gpp.xml  available under/opt/opennms/etc/examples/mcg  to above path.   


6.	All reports/graph definitions are in the following files.
	- opennms/etc/snmp-graph-properties.d/juniper-mcg-pgw-graph.properties 
           NOTE: Please remove /opt/opennms/etc/snmp-graph-properties.d/3gpp.properties if   
           exists and copy the above file.
             
7.	Restart opennms by typing following service commands

Service jmp-watchdog stop
Service jmp-opennms stop
Service jmp-watchdog start

8.	Login to Junos Space, select Network Monitoring, Reports, resource graphs (or KSC) and the corresponding device to see the reports corresponding to the selection. 

Note: If you do not see graphs or see empty reports, collection is not working. Please check  /opt/opennms/share/rrd/snmp. A directory will appear for each node (node-id is the directory name) and all the stats are captured in rrd files under that directory. 

