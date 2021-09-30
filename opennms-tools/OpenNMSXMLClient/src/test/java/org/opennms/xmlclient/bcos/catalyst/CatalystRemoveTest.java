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

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import org.junit.Test;
import com.bt.bcos.adapter.AdapterIf;
import org.opennms.xmlclient.bcos.OpenNmsBcosAdapter;

public class CatalystRemoveTest {

	@Test
	public void removetest1(){
		AdapterIf bcosAdapter = new OpenNmsBcosAdapter();
		
		String service="";
		String machine_ident="i-bdfe83d6";
		
		assertTrue (bcosAdapter.remove(service, machine_ident));

	}
	
	@Test
	public void removetest2(){
		AdapterIf bcosAdapter = new OpenNmsBcosAdapter();
		
		String service="";
		String machine_ident="ec2-204-236-212-140.compute-1.amazonaws.com";
		
		assertTrue (bcosAdapter.remove(service, machine_ident));

	}
	
}
