/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.svclayer.catstatus.model;

import java.util.Collection;
import java.util.ArrayList;

/**
 * <p>StatusInterface class.</p>
 *
 * @author <a href="mailto:jason.aras@opennms.org">Jason Aras</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class StatusInterface {
	
	private String m_ipaddress;
	private String m_interfacename;
	private Collection<StatusService> m_services;
	
	/**
	 * <p>Constructor for StatusInterface.</p>
	 */
	public StatusInterface(){
		
		m_services = new ArrayList<>();
		
	}
	
	/**
	 * <p>addService</p>
	 *
	 * @param service a {@link org.opennms.web.svclayer.catstatus.model.StatusService} object.
	 */
	public void addService ( StatusService service ) {	
		m_services.add(service);
	}
	
	/**
	 * <p>getServices</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<StatusService> getServices(){
		return m_services;
	}
	
	/**
	 * <p>getInterfacename</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getInterfacename() {
		return m_interfacename;
	}
	
	/**
	 * <p>setInterfacename</p>
	 *
	 * @param m_interfacename a {@link java.lang.String} object.
	 */
	public void setInterfacename(String m_interfacename) {
		this.m_interfacename = m_interfacename;
	}
	
	/**
	 * <p>getIpAddress</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getIpAddress() {
		return m_ipaddress;
	}
	
	/**
	 * <p>setIpAddress</p>
	 *
	 * @param m_ipaddress a {@link java.lang.String} object.
	 */
	public void setIpAddress(String m_ipaddress) {
		this.m_ipaddress = m_ipaddress;
	}
	
	

}
