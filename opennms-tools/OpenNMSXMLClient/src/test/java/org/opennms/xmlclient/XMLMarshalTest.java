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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XMLMarshalTest {

	@Test
	public void testMarshal() {
       
       org.opennms.client.schema.ModelImport modelImport = new org.opennms.client.schema.ModelImport();
       
       modelImport.setForeignSource("imported:BTForeignSource");
       
       org.opennms.client.schema.Node vNode= new org.opennms.client.schema.Node();
       org.opennms.client.schema.Interface vInterface = new  org.opennms.client.schema.Interface();
       org.opennms.client.schema.MonitoredService vMonitoredService = new org.opennms.client.schema.MonitoredService();
       vMonitoredService.setServiceName("Apache-Stats");
       
       vInterface.addMonitoredService(vMonitoredService);
       vInterface.setIpAddr("127.0.0.1");
       vNode.addInterface(vInterface);
       
       modelImport.addNode(vNode);
       java.io.Writer out = new java.io.StringWriter();

       try {
           modelImport.marshal( out);
       } catch (Throwable ex){
    	   System.out.println(ex);
       }
       System.out.println("returned value"+ out.toString().replace("<", "\n<"));
       
	}

}
