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

package com.bt.bcos.adapter;

import java.util.HashMap;

/**
 * Description: 
 * defines the interface that talks to the local monitoring platform; bcos or koala 
 * When a LAMP deployment is set up the interface below will be called with
 * as following **PER Apache Host**:
 * 
 * add(String service, HashMap<String, String> params)
 * params 
 * key              value                         
 * "description",   "<a textual description>" 
 * "machine_ident", "<a unique identifier>" 
 * "ip_address",    "<IP address>" 
 * "http_listen_port",  "<HTTP listen port, typically "80" 
 * "https_listen_port", "<HTTPS listen port, typically "443"
 * 
 * The commit method will be called after the entire deployment has been
 * configured but you can probably ignore that.
 * 
 * The add method will be called again when a new Apache Host is added to
 * the deployment (scale-up). And commit will be called again.
 * 
 * The remove method will be called at scale-down or destroy deployment as
 * follows, again **PER Apache Host**:
 * remove("sytheticSRT", <a unique identifier>)
 * where the unique identifier is the machine_ident parameter in the add
 * method that added that host.
 */
public interface AdapterIf {

	/**
	 * @param service monitoring service to be set up (e.g. HTTP_Server_Response_Time)
	 * @param params set of parameters which the adaptor will understand and use to set up the required monitoring
	 *        e.gIP address, hostname, alias
	 * @return true if successful
	 */
	public boolean add(String service, HashMap<String,String> params);

	/**
	 * 
	 * @param service monitoring service to be taken down.
	 * @param resourceIdent the key which allows removal of the monitoring service.
	 * @return  true if successful
	 */
	public boolean remove(String service, String resourceIdent);

	/**
	 * Some monitoring systems (e.g. Nagios) require a commit after a block of 
	 * operations to pick up the changes. Others might just ignore this call.
	 * @return  true if successful
	 */
	public boolean commit(); 

}
