                                                                 MBG PM Setup Details:                                                

1.	The examples directory (/opt/opennms/etc/examples) is delivered as part of OpenNMS to refer to sample configs and see how to use them in their own environments.  

2.	For PM Config, the following are the files to be configured. 
      
    	/opt/opennms/etc/collectd-configuration.xml 
-	Copy the MBG_collectd-configuration.xml available under examples directory and edit it 
-	Add the IP addresses of the devices to collect data under the desired packages 
	
	There are 3 bundles packages which can be used for collection.
-	MBG-KPIs - Only collect those stats which are used to calculate the bundled KPIs/reports (both PGW/SGW stats). 
	o	Select this if you want to just see the bundled reports. 
-	MBG-PGW - collect all PGW stats from the devices 
	o	Select this option if you would like to add a lot more reports than what is available by default for GGSN/PGWs.
	o	Do not select MBG-KPIs package if you select this(will cause duplicates)

-	MBG-SGW - collect all SGW stats from the devices 
	o	Select this option if you would like to add a lot more reports than what is available by default for SGWs
	o	Do not select MBG-KPIs package if you select this(will cause duplicates)

        Change the service status of the SNMP collection to on for the selected package.    
                By default, it is turned on for MBG-KPIs. 
                service name="SNMP" interval="300000" user-defined="false" status="on"> 
	- Save the changes into openms/etc/collectd-configuration.xml 
	- You may want to tune the retry and timeout values to desired values if you see timeouts happening during data collection (shows up as an event under Events) 

	opennms/etc/datacollection-config.xml 
        - Copy the MBG_datacollection-config.xml in examples directory to this file. 

	opennms/etc/snmp-config.xml 
	- The parameters used to connect with SNMP agents are defined in this file.
         
3.	All other configurations should be in place 
	- opennms/etc/datacollection/juniper_mbg_pgw.xml 
	- opennms/etc/datacollection/juniper_mbg_sgw.xml
	- opennms/etc/datacollection/juniper_mbg_kpis.xml     
            These have the details of the stats to be collected for the respective packages 
            described above. The resource labels and tables are also defined in these files.

4.	All reports/graph definitions are in the following files.
	- opennms/etc/snmp-graph-properties.d/juniper-mbg-pgw-graph.properties 
	- opennms/etc/snmp-graph-properties.d/juniper-mbg-sgw-graph.properties 
             
5.	Restart opennms by typing service opennms restart or restart Junos Space
6.	Login to Junos Space, select Network Monitoring, Reports, resource graphs(or KSC) and the corresponding device to see the reports corresponding to the selection. 

Note: If you do not see graphs or see empty reports, collection is not working. Please check  /opt/opennms/share/rrd/snmp. A directory will appear for each node (node-id is the directory name) and all the stats are captured in rrd files under that directory. 

                                                                 MBG FM Setup Details:         


1. Copy(overwrite) the all converted xml files under the below mentioned folder of openNMS configuration.

	/opt/opennms/etc/events/JuniperEvents/mbg

2. Copy(overwrite) the eventconf.xml  file under the below mentioned folder of openNMS configuration.

	/opt/opennms/etc/
  
3. Restart opennms by typing service opennms restart or restart JS 
              
