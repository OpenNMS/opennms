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

public class RequisitionRemoveTest {

	@Test
	public void requisitionRemoveTest(){
		ClientPropertiesLoader clientProps = new ClientPropertiesLoader();
		clientProps.setPropertiesFilename("onmsxmlclient.properties");
		
		OpenNmsXmlClient onmsClient = new OpenNmsXmlClient();
		
		onmsClient.setOpennmsUrl(clientProps.getOpennmsUrl());
		onmsClient.setPassword(clientProps.getPassword());
		onmsClient.setUsername(clientProps.getUsername());
		String foreign_source = clientProps.getForeign_source();// "imported:TestForeignSource1";
		
		// Remove a requisition with the given foreign source.
		assertTrue (onmsClient.requisitionRemove(foreign_source, false));
	}
}

/*
 *             requisition remove <foreign-source> [deployed]
                    Remove the requisition with the given foreign source.

                    If the optional argument "deployed" is specified, it
                    will remove the already-imported foreign source
                    configuration.
                    
                    
//NOTE: http://stackoverflow.com/questions/1051004/how-to-send-put-delete-http-request-in-http urlconnection-looks-like-not-workin
http://stackoverflow.com/questions/1051004/how-to-send-put-delete
To perform an HTTP PUT:

URL url = new URL("http://www.example.com/resource");
HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
httpCon.setDoOutput(true);
httpCon.setRequestMethod("PUT");
OutputStreamWriter out = new OutputStreamWriter(
    httpCon.getOutputStream());
out.write("Resource content");
out.close();

To perform an HTTP DELETE:

URL url = new URL("http://www.example.com/resource");
HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
httpCon.setDoOutput(true);
httpCon.setRequestProperty(
    "Content-Type", "application/x-www-form-urlencoded" );
httpCon.setRequestMethod("DELETE");
httpCon.connect();

               

 */
