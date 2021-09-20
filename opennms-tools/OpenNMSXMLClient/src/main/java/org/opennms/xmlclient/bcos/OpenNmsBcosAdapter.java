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

package org.opennms.xmlclient.bcos;

import java.util.HashMap;
import java.util.Iterator;

import java.net.URL;

import org.opennms.xmlclient.BasicHttpMethods;
import org.opennms.xmlclient.ClientPropertiesLoader;
import org.opennms.xmlclient.OpenNmsXmlClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 
import com.bt.bcos.adapter.AdapterIf;

/**
 * OpenNMSBcosAdapter
 * This class implements the com.bt.bcos.adapter.AdapterIf interface as an adaptor to
 * the OpenNMS REST provisioning interface. 
 * 
 * add(String service, HashMap<String, String> params)
 * params 
 * key              value                          What happens in OpenNMS
 * "description",   "<a textual description>"      Added to node description inventory record
 * "machine_ident", "<a unique identifier>"        Set as foreign ID and OpenNMS Node Label
 * "ip_address",    "<IP address>"                 set as IP address of first port on node
 * "http_listen_port",  "<HTTP listen port, typically "80"    NOT USED - automatically discovered
 * "https_listen_port", "<HTTPS listen port, typically "443"  NOT USED - trial not using Https
 * 
 */
public class OpenNmsBcosAdapter implements AdapterIf {
	private Log log = LogFactory.getLog(OpenNmsBcosAdapter.class.getName());

	private String opennmsUrl;       // URL to address opennms e.g "http://localhost:8980";
	private String username;         // user ID for accessing REST interface e.g "admin"; 
	private String password;         // password for accessing REST interface  e.g "admin";
	private String foreign_source;   // foreign source for importing data e.g  "imported:BTForeignSource";

	private static String requisitionCmd="/opennms/rest/requisitions"; // OpenNMS REST base for requisition commands

	private static String propertiesFilename="onmsbcosclient.properties"; // file name of properties file used by this adaptor

	private OpenNmsXmlClient onmsXmlClient;

	/**
	 * constructor
	 */
	public OpenNmsBcosAdapter(){

		ClientPropertiesLoader clientProps = new ClientPropertiesLoader();
		clientProps.setLog(log);
		clientProps.setPropertiesFilename(propertiesFilename);

		opennmsUrl     = clientProps.getOpennmsUrl();
		password       = clientProps.getPassword();
		username       = clientProps.getUsername();
		foreign_source = clientProps.getForeign_source();

		log.info("OpenNmsBcosAdapter() using properties: foreign_source='"+foreign_source
				+"', opennmsUrl='"+opennmsUrl+"', username='"+username+"', password='"+password+"'");

		onmsXmlClient = new OpenNmsXmlClient();
		onmsXmlClient.setLog(log);
		onmsXmlClient.setUsername(username);
		onmsXmlClient.setPassword(password);
		onmsXmlClient.setOpennmsUrl(opennmsUrl);
	}

	/**
	 * This method implements the bcos nagios add method. It adds the nagios service to the
	 * node identified in the hash map.

	 * @param nagiosService the service provisioned on the node identified in the requisition
	 * @param params Hash map containing additional information to identify the interface, node and service
	 * @return true if provisioning succeeds false if fails
	 * 
	 * @see com.bt.bcos.adapter.AdapterIf#add(java.lang.String, java.util.HashMap)
	 */
	public boolean add(String nagiosService, HashMap<String, String> params) {
		//makes call to local adapter for platform based on IP address

		log.debug("Call made to OpenNMSAdapter to add a node and service to a requisition. nagiosService=" + nagiosService);
		log.debug("params HashMap contents");
		Iterator<String> iterator = params.keySet().iterator();
		while(iterator.hasNext()) {
			String key = iterator.next().toString();  
			String val  = params.get(key);  
			log.debug("   Key is: " + key + "\t\t  Value is : " + val);
		}

		String description = params.get("description");
		String machine_ident = params.get("machine_ident");
		String ip_address= params.get("ip_address");

		try {

			// if requisition does not exist, this will create one. It will not add a new
			// requisition if it already exists
			String requisitions = onmsXmlClient.list();
			if (!requisitions.contains("foreign-source=\""+foreign_source+"\"")) {
				log.debug("nodeAdd() foreign source: '"+foreign_source+"' does not exist. Adding new foreign source to OpenNMS");
				if (onmsXmlClient.requisitionAdd(foreign_source)==false) {
					log.debug("nodeAdd() failed to add foreign source: "+foreign_source);
					return false;
				}
			} else log.debug("nodeAdd() foreign source: "+foreign_source+" already exists");

			// the same node can be defined in a requisition several times. We want to only have one node definition for
			// each node. This will prevent more than one node with the same name being defined
			if (requisitions.contains("foreign-id=\""+machine_ident+"\"")) {
				log.debug("nodeAdd() foreign-id: '"+machine_ident+"' already exists in requisition. Remove node before adding a new one");
				return false;
			}

			// this will add a new node with the specified interface to the requisition
			org.opennms.client.schema.Interface vInterface= new org.opennms.client.schema.Interface();
			vInterface.setIpAddr(ip_address);

			org.opennms.client.schema.Node vNode= new org.opennms.client.schema.Node();
			vNode.setForeignId(machine_ident);
			vNode.setNodeLabel(machine_ident);
			vNode.addInterface(vInterface);

			java.io.Writer marshalledStr = new java.io.StringWriter();
			vNode.marshal( marshalledStr);
			log.debug("nodeAdd() XML message sent: "+ marshalledStr.toString().replace("<", "\n<"));

			java.io.Reader data = new java.io.StringReader(marshalledStr.toString());
			java.io.Writer output= new java.io.StringWriter();
			URL url = new URL(opennmsUrl+requisitionCmd+"/"+foreign_source+"/nodes");
			
			BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
			basicHttpMethods.setLog(log);
			basicHttpMethods.postData(data, url, output, username, password);

			log.debug("OpenNMSBcosAdapter add() Reply: "+ output.toString());

		} catch (Throwable e){
			log.error("OpenNMSBcosAdapter add() command error: ",e);
			return false;
		}

		return true;

	}

	/**
	 * This method implements the bcos nagios remove method.
	 * It removes an OpenNMS monitored service from a node in a requisition 
	 * @param nagiosService the service provisioned on the node identified in the requisition
	 * @param machine_ident the node name of the node as identified in the requisition
	 * string nagios
	 * @return true if provisioning succeeds false if fails
	 * @see com.bt.bcos.adapter.AdapterIf#remove(java.lang.String, java.lang.String)
	 */
	public boolean remove(String nagiosService, String machine_ident) {
		log.debug("Call made to OpenNMSAdapter to remove a node and service from a requisition. nagiosService=" + nagiosService+" machine_ident="+ machine_ident);

		try{
			if (onmsXmlClient.nodeRemove(foreign_source, machine_ident)==false) {
				log.error("OpenNMSBcosAdapter remove machine_ident= "+machine_ident+" failed ");
				return false;
			}
		} catch (Throwable e){
			log.error("OpenNMSBcosAdapter remove() command error: ",e);
			return false;
		}
		return true;
	}

	/** 
	 * This method implements the restartNagios method.
	 * It loads the provisioned requisition and changes the  OpenNMS configuration 
	 * without restarting OpenNMS
	 * @return true if requisition is accepted into OpenNNS. False if fails
	 * @see com.bt.bcos.adapter.AdapterIf#commit()
	 */
	public boolean commit() {
		log.debug("restartNagios Call made to OpenNMSAdapter to re-load requisition. ");

		try{
			if (onmsXmlClient.requisitionImport(foreign_source)==false) {
				log.error("OpenNMSBcosAdapter import foreign_source= "+foreign_source+" failed ");
				return false;
			}

		} catch (Throwable e){
			log.error("OpenNMSBcosAdapter restart() command error: ",e);
			return false;
		}

		return true;
	}


	/**
	 * This main class provides example usage for BCOS adaptor and simple test commmands
	 * @param args
	 */
	public static void main(String[] args) {
		String helpstring="OpenNMS Bcos adapter\n"
			+             "--------------------\n"
			+"This program provides a simple CLI client to test the OpenNMSBcosAdapter class."
			+"You should provide a "+propertiesFilename+" file in the same directory as this jar\n"
			+"Usage:\n"
			+"   -help: print this help message\n"
			+"   -addNode <machine_ident> <ipaddress> <description>: adds the node, interface & description for node identified by machine_ident\n"
			+"   -removeNode <machine_ident> : removes the node identified by machine_ident\n"
			+"   -restart : loads the new requisition configuration into OpenNMS\n"		;

		if ((args.length==0) || "-help".equals(args[0])) {
			System.out.println(helpstring);

		} else if("-addNode".equals(args[0])){
			if(args.length!=4) {
				System.out.println("Error - incorrect number of arguments\n"+helpstring);
			} else {
				AdapterIf bcosAdapter= new OpenNmsBcosAdapter();
				String nagiosService="";
				HashMap<String,String> params = new HashMap<String,String>();
				params.put("machine_ident", args[1]);  //  Set as foreign ID and OpenNMS Node Label
				params.put("ip_address",    args[2]);  //  Set as IP address of first port on node
				params.put("description",   args[3]);  //  Added to node description inventory record

				if (bcosAdapter.add(nagiosService, params)==true){
					System.out.println("OpenNMSBcosAdapter: Success interface added node");
				} else System.out.println("OpenNMSBcosAdapter: Failure - interface did not add node");
			}
		} else if("-removeNode".equals(args[0])){
			if(args.length!=2) {
				System.out.println("Error - incorrect number of arguments\n"+helpstring);
			} else {
				String nagiosService="";
				String machine_ident=args[1];
				AdapterIf bcosAdapter= new OpenNmsBcosAdapter();
				if (bcosAdapter.remove(nagiosService, machine_ident)==true){
					System.out.println("OpenNMSBcosAdapter: Success removed node from requisiton");
				} else System.out.println("OpenNMSBcosAdapter: Failure - did not remove node from requisiton");
			}

		} else if("-restart".equals(args[0])){
			AdapterIf bcosAdapter= new OpenNmsBcosAdapter();
			if (bcosAdapter.commit()==true){
				System.out.println("OpenNMSBcosAdapter: Success loaded requisiton");
			} else System.out.println("OpenNMSBcosAdapter: Failure - did not load");

		} else System.out.println("Unknown Command. Use the following instructions:\n"+helpstring);








	}

}
