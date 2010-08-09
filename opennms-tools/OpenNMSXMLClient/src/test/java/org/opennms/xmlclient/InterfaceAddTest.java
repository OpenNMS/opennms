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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.bt.bcos.adapter.AdapterIf;

public class InterfaceAddTest {

	@Test
	public void interfaceAddTest(){

		ClientPropertiesLoader clientProps = new ClientPropertiesLoader();
		clientProps.setPropertiesFilename("onmsxmlclient.properties");
		
		OpenNmsXmlClient onmsClient = new OpenNmsXmlClient();
		
		onmsClient.setOpennmsUrl(clientProps.getOpennmsUrl());
		onmsClient.setPassword(clientProps.getPassword());
		onmsClient.setUsername(clientProps.getUsername());
		String foreign_source = clientProps.getForeign_source();// "imported:TestForeignSource1";

		// Add a requisition with the given foreign source.
		String foreign_id="node1";
		String ip_address="192.168.0.250";

		assertTrue (onmsClient.interfaceAdd(foreign_source, foreign_id, ip_address));

	}
}

