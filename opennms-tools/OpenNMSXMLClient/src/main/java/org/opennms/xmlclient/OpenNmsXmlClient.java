/* 
 * Licensed to the OpenNMS Group Inc. under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The OpenNMS Group Inc. licences this file to You under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opennms.xmlclient;

import java.util.HashMap;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 

import org.opennms.xmlclient.BasicHttpMethods;


/**
 * OpenNmsXmlclient
 * This class implements in java similar functionality to the provision.pl script
 */
public class OpenNmsXmlClient  {
	private Log log = LogFactory.getLog(OpenNmsXmlClient.class.getName());

	private String opennmsUrl    = "http://localhost:8980"; // default URL to address opennms
	private String username      = "admin";   // default user ID for accessing REST interface
	private String password      = "admin";   // default user password for accessing REST interface

	private static String requisitionCmd="/opennms/rest/requisitions"; // default OpenNMS REST command base for requisition commands 


	// RAW REQUISITION COMMANDS
	/**
	 * list    List the available requisition foreign sources.
	 */
	public String list(){
		String result="";
		try{
			// get changed requisition
			BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
			basicHttpMethods.setLog(log);
			result = basicHttpMethods.sendGetRequest(opennmsUrl+requisitionCmd,"", username, password);
			log.debug("Requisition after command is : " + result.replace("<", "\n<"));
		} catch (Throwable e){
			log.error("OpenNmsXmlClient add() command error: ",e);
			return "failed to get result; Error: "+e;
		}
		return result;
	}


	/**
	 * requisition add <foreign-source>
	 * Add a requisition with the given foreign source.
	 */
	public boolean requisitionAdd(String foreign_source){

		try {
			org.opennms.client.schema.ModelImport modelImport = new org.opennms.client.schema.ModelImport();
			modelImport.setForeignSource(foreign_source);
			java.io.Writer marshalledStr = new java.io.StringWriter();
			modelImport.marshal( marshalledStr);
			log.debug("requisitionAdd() XML message sent: "+ marshalledStr.toString().replace("<", "\n<"));

			java.io.Reader data = new java.io.StringReader(marshalledStr.toString());
			java.io.Writer output= new java.io.StringWriter();
			URL url = new URL(opennmsUrl+requisitionCmd);
			
			BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
			basicHttpMethods.setLog(log);
			basicHttpMethods.postData(data, url, output, username,password);

			log.debug("requisitionAdd() Reply: "+ output.toString());
		} catch (Throwable ex){
			log.debug("requisitionAdd() Failure sending message Error: ", ex);
			return false;
		}
		return true;
	}

	/**
	 * requisition remove <foreign-source> [deployed]
	 * Remove the requisition with the given foreign source.
	 * If the optional argument "deployed" is specified, it
	 * will remove the already-imported foreign source
	 * configuration.
	 */
	public boolean requisitionRemove(String foreign_source, boolean deployed){
		try {
			java.io.Writer output= new java.io.StringWriter();
			URL url;
			if(deployed){
				url = new URL(opennmsUrl+requisitionCmd+"/deployed/"+foreign_source);
			} else {
				url = new URL(opennmsUrl+requisitionCmd+"/"+foreign_source);
			}
			BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
			basicHttpMethods.setLog(log);
			basicHttpMethods.deleteData(url, output,username, password);

			log.debug("requisitionRemove() Reply: "+ output.toString());
		} catch (Throwable ex){
			log.debug("requisitionRemove() Failure sending message Error: ", ex);
			return false;
		}
		return true;
	}

	/**
	 * requisition import <foreign-source Import the requisition with the given foreign source.
	 */ 
	public boolean requisitionImport(String foreign_source){
		try {
			java.io.Reader data = new java.io.StringReader("");
			java.io.Writer output= new java.io.StringWriter();
			URL url = new URL(opennmsUrl+requisitionCmd+"/"+foreign_source+"/import");
			
			BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
			basicHttpMethods.setLog(log);
			basicHttpMethods.putData(data, url, output, username, password);

			log.debug("requisitionImport() Reply: "+ output.toString());
		} catch (Throwable ex){
			log.debug("requisitionImport() Failure sending message Error: ", ex);
			return false;
		}
		return true;
	}

	/**
	 * Add a node to the requisition identified by the given foreign source.
	 * @param foreign_source foreign source identifier
	 * @param foreign_id     node id used in foreign source
	 * @param node_label     node label to use to describe node
	 * @return
	 */
	public boolean nodeAdd(String foreign_source, String foreign_id, String node_label){
		try {

			org.opennms.client.schema.Node vNode= new org.opennms.client.schema.Node();
			vNode.setForeignId(foreign_id);
			vNode.setNodeLabel(node_label);

			java.io.Writer marshalledStr = new java.io.StringWriter();
			vNode.marshal( marshalledStr);
			log.debug("nodeAdd() XML message sent: "+ marshalledStr.toString().replace("<", "\n<"));

			java.io.Reader data = new java.io.StringReader(marshalledStr.toString());
			java.io.Writer output= new java.io.StringWriter();
			URL url = new URL(opennmsUrl+requisitionCmd+"/"+foreign_source+"/nodes");
			
			BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
			basicHttpMethods.setLog(log);
			basicHttpMethods.postData(data, url, output, username, password);

			log.debug("nodeAdd() Reply: "+ output.toString());
		} catch (Throwable ex){
			log.debug("nodeAdd() Failure sending message Error: ", ex);
			return false;
		}
		return true;
	}

	/**
	 * Remove a node from the requisition identified by the given foreign source and foreign ID.
	 * @param foreign_source foreign source identifier
	 * @param foreign_id     node id used in foreign source
	 * @return
	 */
	public boolean nodeRemove(String foreign_source, String foreign_id){
		try {
			java.io.Writer output= new java.io.StringWriter();
			URL url= new URL(opennmsUrl+requisitionCmd+"/"+foreign_source+"/nodes/"+foreign_id);

			BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
			basicHttpMethods.setLog(log);
			basicHttpMethods.deleteData(url, output, username, password);

			log.debug("nodeRemove() Reply: "+ output.toString());
		} catch (Throwable ex){
			log.debug("nodeRemove() Failure sending message Error: ", ex);
			return false;
		}
		return true;
	}


	/**
	 * Set a property on a node, given the foreign source and foreign id. Valid properties are:
	 *    building
	 *    city
	 *    node-label
	 *    parent-foreign-id
	 *    parent-node-label
	 *    
	 * @param foreign_source foreign source identifier
	 * @param foreign_id     node id used in foreign source
	 * @param properties
	 * @return
	 */
	public boolean nodeSet(String foreign_source, String foreign_id, HashMap<String,String> properties){
		//TODO write 
		return false;
	}

	/**
	 * Add an interface to the requisition identified by the given foreign source and node foreign ID.
	 * @param foreign_source foreign source identifier
	 * @param foreign_id     node id used in foreign source
	 * @param ip_address     ip address of the interface
	 * @return
	 */
	public boolean interfaceAdd(String foreign_source, String foreign_id, String ip_address){
		try {

			org.opennms.client.schema.Interface vInterface= new org.opennms.client.schema.Interface();
			vInterface.setIpAddr(ip_address);

			java.io.Writer marshalledStr = new java.io.StringWriter();
			vInterface.marshal( marshalledStr);
			log.debug("interfaceAdd() XML message sent: "+ marshalledStr.toString().replace("<", "\n<"));

			java.io.Reader data = new java.io.StringReader(marshalledStr.toString());
			java.io.Writer output= new java.io.StringWriter();
			URL url = new URL(opennmsUrl+requisitionCmd+"/"+foreign_source+"/nodes/"+foreign_id+"/interfaces");
			
			BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
			basicHttpMethods.setLog(log);
			basicHttpMethods.postData(data, url, output, username, password);

			log.debug("interfaceAdd() Reply: "+ output.toString());
		} catch (Throwable ex){
			log.debug("interfaceAdd() Failure sending message Error: ", ex);
			return false;
		}
		return true;
	}

	/**
	 * Remove an interface from the requisition identified by the given foreign source, foreign ID, and IP address.
	 * @param foreign_source foreign source identifier
	 * @param foreign_id     node id used in foreign source
	 * @param ip_address     ip address of the interface
	 * @return
	 */
	public boolean interfaceRemove(String foreign_source, String foreign_id, String ip_address){
		//$foreign_source . '/nodes/' . $foreign_id . '/interfaces/' . $ip
		try {
			java.io.Writer output= new java.io.StringWriter();
			URL url= new URL(opennmsUrl+requisitionCmd+"/"+foreign_source+"/nodes/"+foreign_id+"/interfaces/"+ip_address);

			BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
			basicHttpMethods.setLog(log);
			basicHttpMethods.deleteData(url, output, username, password);

			log.debug("interfaceRemove() Reply: "+ output.toString());
		} catch (Throwable ex){
			log.debug("interfaceRemove() Failure sending message Error: ", ex);
			return false;
		}
		return true;
	}

	/**
	 *  Set a property on an interface, given the foreign source, foreign id, and IP address. 
	 *  Valid properties are:
	 *      descr - the interface description
	 *      snmp-primary - P (primary), S (secondary), N (not eligible)
	 *      status - 1 for managed, 3 for unmanaged (yes, I know)
	 * @param foreign_source foreign source identifier
	 * @param foreign_id     node id used in foreign source
	 * @param ip_address     ip address of the interface
	 * @param properties     key value pair defining properties applied to interface
	 * @return
	 */
	public boolean interfaceSet(String foreign_source, String foreign_id, String ip_address, HashMap<String,String> properties){
		//TODO write 
		return false;
	}


	/* *****************************************
	 * GETTERS AND SETTERS FOR CLASS PROPERTIES
	 * *****************************************/

	public Log getLog() {
		return log;
	}

	/**
	 * @param log set commons Log for this class. Defaults to OpenNmsXmlClient.class.getName() if not set.
	 */
	public void setLog(Log log) {
		this.log = log;
	}

	/**
	 * @return url used to access opennms instance
	 */
	public String getOpennmsUrl() {
		return opennmsUrl;
	}

	/**
	 * @param opennmsUrl URL to address opennms. Defaults to "http://localhost:8980" if not set. 
	 */
	public void setOpennmsUrl(String opennmsUrl) {
		this.opennmsUrl = opennmsUrl;
	}

	/**
	 * @return username user ID for accessing REST interface
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username user ID for accessing REST interface. Defaults to "admin" if not set. 
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return password user password for accessing REST interface
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password user password for accessing REST interface. Defaults to "admin" if not set. 
	 */
	public void setPassword(String password) {
		this.password = password;
	}





}
/*  provison.pl commands - for reference as class is written
	 Options:
		    --help  Print a brief help message and exit.

		    --version
		            Print the version and exit.

		    --username
		            The username to use when connecting to the RESTful API. This
		            user must have administrative privileges in the OpenNMS web UI.

		            Defaults to 'admin'.

		    --password
		            The password associated with the administrative username
		            specified in -username.

		            Defaults to 'admin'.

		    --url   The URL of the OpenNMS REST interface. Defaults to
		            'http://localhost:8980/opennms/rest'.

		Arguments:
		    list    List the available requisition foreign sources.

		    requisition

		            requisition add <foreign-source>
		                    Add a requisition with the given foreign source.

		            requisition remove <foreign-source> [deployed]
		                    Remove the requisition with the given foreign source.

		                    If the optional argument "deployed" is specified, it
		                    will remove the already-imported foreign source
		                    configuration.

		            requisition import <foreign-source>
		                    Import the requisition with the given foreign source.

		    node

		            node add <foreign-source> <foreign-id> <node-label>
		                    Add a node to the requisition identified by the given
		                    foreign source.

		            node remove <foreign-source> <foreign-id>
		                    Remove a node from the requisition identified by the
		                    given foreign source and foreign ID.

		            node set <foreign-source> <foreign-id> <key> [value]
		                    Set a property on a node, given the foreign source and
		                    foreign id. Valid properties are:

 * building
 * city
 * node-label
 * parent-foreign-id
 * parent-node-label

		    interface

		            interface add <foreign-source> <foreign-id> <ip-address>
		                    Add an interface to the requisition identified by the
		                    given foreign source and node foreign ID.

		            interface remove <foreign-source> <foreign-id> <ip-address>
		                    Remove an interface from the requisition identified by
		                    the given foreign source, foreign ID, and IP address.

		            interface set <foreign-source> <foreign-id> <ip-address> <key>
		            [value]
		                    Set a property on an interface, given the foreign
		                    source, foreign id, and IP address. Valid properties
		                    are:

 * descr - the interface description
 * snmp-primary - P (primary), S (secondary), N (not eligible)
 * status - 1 for managed, 3 for unmanaged (yes, I know)

		    service

		            service add <foreign-source> <foreign-id> <ip-address>
		            <service-name>
		                    Add a service to the interface identified by the given
		                    foreign source, node ID, and IP address.

		            service remove <foreign-source<gt <foreign-id> <ip-address>
		            <service-name>>
		                    Remove a service from the interface identified by the
		                    given foreign source, node ID, and IP address.

		    category

		            category add <foreign-source> <foreign-id> <category-name>
		                    Add a category to the node identified by the given
		                    foreign source and node foreign ID.

		            category remove <foreign-source> <foreign-id> <category-name>
		                    Remove a category from the node identified by the given
		                    foreign source and node foreign ID.

		    asset

		            asset add <foreign-source> <foreign-id> <key> [value]
		                    Add an asset to the node identified by the given foreign
		                    source and node foreign ID.

		            asset remove <foreign-source> <foreign-id> <key>
		                    Remove an asset from the node identified by the given
		                    foreign source and node foreign ID.

		            asset set <foreign-source<gt <foreign-id> <key> <value>>
		                    Set an asset value given the node foreign source,
		                    foreign ID, and asset key.

		    snmp

		            snmp get <ip-address>
		                    Get the SNMP configuration for the given IP address.

		            snmp set <ip-address> <community> [options...]
		                    Set the SNMP community (and, optionally, version) for
		                    the given IP address.

		                    Optionally, you can set additional options as key=value
		                    pairs. For example:

		                            snmp set 192.168.0.1 public version=v1 timeout=1000

		                    Valid options are:

 * version: v1 or v2c
 * port: the port of the SNMP agent
 * timeout: the timeout, in milliseconds
 * retries: the number of retries before giving up


 */


