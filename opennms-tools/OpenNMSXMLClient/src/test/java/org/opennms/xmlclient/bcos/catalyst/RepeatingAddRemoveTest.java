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

package org.opennms.xmlclient.bcos.catalyst;

import org.junit.Test;
import static org.junit.Assert.*;
import com.bt.bcos.adapter.AdapterIf;
import java.util.HashMap;
import org.opennms.xmlclient.bcos.OpenNmsBcosAdapter;

/**
 * Test of adding and removing relanodes from catalyst
 * <node node-label="ec2-204-236-212-140.compute-1.amazonaws.com" foreign-id="ec2-204-236-212-140.compute-1.amazonaws.com" building="">
 *      <interface snmp-primary="" ip-addr="204.236.212.140" descr=""/>
 *   </node>
 *  <node node-label="i-bdfe83d6" foreign-id="i-bdfe83d6">
 *      <interface ip-addr="184.73.92.108"/>
 *  </node>
 * @author openoss
 *
 */
public class RepeatingAddRemoveTest {

	@Test
	public void addnode1(){
		AdapterIf bcosAdapter = new OpenNmsBcosAdapter();

		String service="";
		HashMap<String,String> params = new HashMap<String,String>()
		{
			{
				put("description",   "this is a first test node"); //  Added to node description inventory record
				put("machine_ident", "i-bdfe83d6");             //  Set as foreign ID and OpenNMS Node Label
				put("ip_address",    "184.73.92.108");       //  Set as IP address of first port on node
				put("http_listen_port",  "80" );             //  NOT USED - automatically discovered
				put("https_listen_port", "443" );            // NOT USED - trial not using Https
			}
		};
		assertTrue (bcosAdapter.add(service, params));
	}

	@Test
	public void addnode2(){
		AdapterIf bcosAdapter = new OpenNmsBcosAdapter();

		String service="";
		HashMap<String,String> params = new HashMap<String,String>()
		{
			{
				put("description",   "this is a second test node"); //  Added to node description inventory record
				put("machine_ident", "ec2-204-236-212-140.compute-1.amazonaws.com");             //  Set as foreign ID and OpenNMS Node Label
				put("ip_address",    "204.236.212.140");       //  Set as IP address of first port on node
				put("http_listen_port",  "80" );             //  NOT USED - automatically discovered
				put("https_listen_port", "443" );            // NOT USED - trial not using Https
			}
		};
		assertTrue (bcosAdapter.add(service, params));
	}
}
