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

import org.junit.Test;
import static org.junit.Assert.*;
import com.bt.bcos.adapter.AdapterIf;
import java.util.HashMap;
import org.opennms.xmlclient.bcos.OpenNmsBcosAdapter;

public class BcosAddTest {

	@Test
	public void addnode1(){
		AdapterIf bcosAdapter = new OpenNmsBcosAdapter();

		String service="";
		HashMap<String,String> params = new HashMap<String,String>()
		{
			{
				put("description",   "this is a first test node"); //  Added to node description inventory record
				put("machine_ident", "BTNode1");             //  Set as foreign ID and OpenNMS Node Label
				put("ip_address",    "192.168.0.201");       //  Set as IP address of first port on node
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
				put("machine_ident", "BTNode2");             //  Set as foreign ID and OpenNMS Node Label
				put("ip_address",    "192.168.0.203");       //  Set as IP address of first port on node
				put("http_listen_port",  "80" );             //  NOT USED - automatically discovered
				put("https_listen_port", "443" );            // NOT USED - trial not using Https
			}
		};
		assertTrue (bcosAdapter.add(service, params));
	}
}
