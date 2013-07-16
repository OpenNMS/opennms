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

public class GetNodesTest  {

	@Test
	public void getNodesTest(){

		ClientPropertiesLoader clientProps = new ClientPropertiesLoader();
		clientProps.setPropertiesFilename("onmsxmlclient.properties");
		String opennmsUrl= clientProps.getOpennmsUrl();
		String password = clientProps.getPassword();
		String username = clientProps.getUsername();
		String foreign_source = clientProps.getForeign_source();// "imported:TestForeignSource1";
		
		BasicHttpMethods basicHttpMethods= new BasicHttpMethods();
		String result = "";
		try {
		   result = basicHttpMethods.sendGetRequest(opennmsUrl+"/opennms/rest/nodes", "", username, password);
	    } catch (Throwable e) {
	    	fail("exception caught in basicHttpMethods: "+e);
	    }
		System.out.println("Returned from Test:'"+this.getClass().getName()+"' :"+result.replace("<", "\n<"));

	}
}




