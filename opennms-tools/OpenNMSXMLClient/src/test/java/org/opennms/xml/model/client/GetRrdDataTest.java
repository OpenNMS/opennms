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

package org.opennms.xml.model.client;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opennms.xmlclient.BasicHttpMethods;
import org.opennms.xmlclient.ClientPropertiesLoader;

/**
 * http://www.opennms.org/wiki/Data_Export
 * This test shows how to get RRD data as XML based upon examples in http://opennms.org/wiki/Data_Export
 * http://172.16.8.100:8180/opennms/summary/results.htm?filterRule=ipaddr+iplike+10.136.123.1&startTime=1184173183&endTime=1185219010&attributeSieve=.*
 * http://172.16.8.100:8180/opennms/summary/results.htm?filterRule=ipaddr+iplike+*.*.*.*&startTime=1184173183&endTime=1185219010&attributeSieve=(ifInOctets|ifOutOctets)
 * 
 * @author cgallen
 */
public class GetRrdDataTest  {

// 
// 
	@Test
	public void getRRDTest(){

		ClientPropertiesLoader clientProps = new ClientPropertiesLoader();
		clientProps.setPropertiesFilename("onmsxmlclient.properties");
		String opennmsUrl= clientProps.getOpennmsUrl();
		String password = clientProps.getPassword();
		String username = clientProps.getUsername();
		String foreign_source = clientProps.getForeign_source();// "imported:TestForeignSource1";
		
		BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
		String result = "";
		
		Long currentepochseconds=new java.util.Date().getTime()/1000;
		
		String endTime = Long.toString(currentepochseconds); // today in seconds since epoch
		String startTime=Long.toString(currentepochseconds- 24*60*60); // one day ago in seconds since epoch
		
		String nodeFilter="ipaddr+iplike+*.*.*.*";
		String attributeSieve=".*";
		//attributeSieve="(icmp|ifOutOctets)"; // how to set up filter for values
		
		String requestParameters="filterRule="+nodeFilter+"&startTime="+startTime+"&endTime="+endTime+"&attributeSieve="+attributeSieve;
		
		try {
		   result = basicHttpMethods.sendGetRequest(opennmsUrl+"/opennms/summary/results.htm", requestParameters, username, password);
	    } catch (Throwable e) {
	    	e.printStackTrace();
	    	fail("exception caught in basicHttpMethods: "+e);
	    }
		System.out.println("Returned from Test:'"+this.getClass().getName()+"' :"+result.replace("<", "\n<"));

	}
}




