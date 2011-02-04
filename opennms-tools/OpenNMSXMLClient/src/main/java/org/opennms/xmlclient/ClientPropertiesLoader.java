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

import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientPropertiesLoader {
	private Log log = LogFactory.getLog(ClientPropertiesLoader.class.getName());

	private String opennmsUrl= "http://localhost:8980"; // URL to address opennms
	private String username  = "admin";   // user ID for accessing REST interface
	private String password  = "admin";   // password for accessing REST interface
	private String foreign_source= "imported:TestForeignSource1"; // foreign source for importing data
	private String propertiesFilename="onmsclient.properties"; // file name of properties file
	
	private Properties properties = null;
	
	private String path="";

	private void loadProperties() {
		properties = new Properties();
		try { 

			// try to Load from the startup directory of application (ex. directory containing the jar) 
			String p=getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
			path= "/"+p.substring(0, p.lastIndexOf("/"));

			java.io.File propfile = new java.io.File( path + "/"+propertiesFilename);
			if (propfile.exists()){
				log.debug("PropertiesLoader() Trying to load properties file '"+propertiesFilename+"' from directory path:"+path);
				java.io.FileInputStream fis = new java.io.FileInputStream(propfile);
				properties.load(fis);
			} else {
				// try to load from class path 
				log.debug("PropertiesLoader() Properties file not in directory path:'"+path+"' Loading properties file '"+propertiesFilename+"' from classpath");
				URL url = ClassLoader.getSystemResource(propertiesFilename);
				properties.load(url.openStream());
			}

			if (properties.getProperty("opennmsUrl")!=null) {
				opennmsUrl= properties.getProperty("opennmsUrl");
			} else log.error ("PropertiesLoader() 'opennmsUrl' property does not exist in '"+propertiesFilename+"' file. Using default value.");

			if (properties.getProperty("username")!=null) {
				username= properties.getProperty("username");
			} else log.error ("PropertiesLoader() 'username' property does not exist in '"+propertiesFilename+"' file. Using default value.");

			if (properties.getProperty("password")!=null) {
				password= properties.getProperty("password");
			} else log.error ("PropertiesLoader() 'password' property does not exist in '"+propertiesFilename+"' file. Using default value.");

			if (properties.getProperty("foreign_source")!=null) {
				foreign_source= properties.getProperty("foreign_source");
			} else log.error ("PropertiesLoader() 'foreign_source' property does not exist in '"+propertiesFilename+"' file. Using default value.");

		} catch (Throwable e) { 
			log.error("PropertiesLoader() unable to load '"+propertiesFilename+"' file from classpath or file path '"+path+"'. Using default properties. ");
		} 
		log.info("PropertiesLoader() using properties: foreign_source='"+foreign_source
				+"', opennmsUrl='"+opennmsUrl+"', username='"+username+"', password='"+password+"'");
	}

	public void setLog(Log log) {
		this.log = log;
	}

	public void setPropertiesFilename(String propertiesFilename) {
		this.propertiesFilename = propertiesFilename;
	}

	public String getOpennmsUrl() {
		if (properties==null) loadProperties();
		return opennmsUrl;
	}

	public String getUsername() {
		if (properties==null) loadProperties();
		return username;
	}

	public String getPassword() {
		if (properties==null) loadProperties();
		return password;
	}

	public String getForeign_source() {
		if (properties==null) loadProperties();
		return foreign_source;
	}

}
