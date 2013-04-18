                                                                         PM Setup Details:                                  
                      
1. The examples directory is delivered as part of OpenNMS to refer to sample configs and see how to use them in their own environments.   
2. Only the "MBG*" files in the example directory are provided by us. Rest came from OpenNMS.   
3. For  PM config, the following are the files to be configured. 
4. opennms/etc/collectd-configuration.xml 
	- Copy the MBG_collectd-configuration.xml under examples and edit it 
	- Add the IP addresses of the devices to collect data under the desired package 
		There are 4 packages 
		- Example1 - collects all stats (PGW/SGW) from the devices 
		- MBG-PGW - collect all PGW stats from the devices (Choose this for your case) 
		- MBG-SGW - collect all SGW stats from the devices 
		- MBG-KPIs - Only collect those stats which are used to calculate KPIs and draw graphs 
	- Change the service status of the SNMP collection to on for the selected package. By default, it is turned on for MBG-KPIs. 
		service name="SNMP" interval="300000" user-defined="false" status="on"> 
	- Save the changes into openms/etc/collectd-configuration.xml 
	- You may want to tune the retry and timeout values to desired values if you see timeouts happening during data collection(shows up as en event under Events) 
5. opennms/etc/datacollection-config.xml 
	- Copy the MBG_datacollection-config.xml in examples directory to this file. 

6. Opennms/etc/snmp-config.xml 
	- The parameters used to connect with SNMP agents are defined in this file.

7. All other configurations should be in place 
	- opennms/etc/datacollection/juniper_mbg_pgw.xml 
	- opennms/etc/datacollection/juniper_mbg_sgw.xml
	- opennms/etc/datacollection/juniper_mbg_kpis.xml     
	- has the details of the counters to be collected.

	- opennms/etc/snmp-graph-properties.d/juniper-mbg-pgw-graph.properties 
	- opennms/etc/snmp-graph-properties.d/juniper-mbg-sgw-graph.properties 
	- has all the graph definitions for PGW. More graphs can be added here or in opennms/etc/snmp-graph.properties file. 

Instruction: Please delete other properties files (except above) under /opt/opennms/etc/snmp- graph.properties.d/ 

 Because opennms  tries to load all the reports under this path so there may be conflict in
displaying graphs. A graph defined in an included file with the same identifier as one in the
 main snmp-graph.properties file will override the one from the main file. Defining the same 
two graphs with the same identifier in two separate files will result in undefined behaviour. 
 One of them will override the other, but it's not defined which one will win. 


8. Restart opennms by typing service opennms restart or restart JS 
9. After a while you should start seeing graphs becoming available under reports(under network monitoring). You can also check /opt/opennms/share/rrd/snmp. 
10. A directory will appear for each node(node-id is the directory name) and all the stats are captured in rrd files under that directory. 


                                                                         FM Setup Details:         


1. Copy(overwrite) the all converted xml files(which is attached in FM_Config_File folder) under the below mentioned folder of openNMS configuration.

	/opt/opennms/etc/events/JuniperEvents/mbg

2. Copy(overwrite) the eventconf.xml  file(which is attached in FM folder) under the below mentioned folder of openNMS configuration.

	/opt/opennms/etc/
  
3. Restart opennms by typing service opennms restart or restart JS 
              
