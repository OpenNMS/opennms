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
package org.opennms.netmgt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * <p>PathElement class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
@Embeddable
public class PathElement implements Serializable {
    private static final long serialVersionUID = 2437052160139305371L;
    
    private String m_ipAddress;
	private String m_serviceName;
	
	/**
	 * <p>Constructor for PathElement.</p>
	 */
	public PathElement() {
		
	}

	/**
	 * <p>Constructor for PathElement.</p>
	 *
	 * @param ipAddress a {@link java.lang.String} object.
	 * @param serviceName a {@link java.lang.String} object.
	 */
	public PathElement(String ipAddress, String serviceName) {
		m_ipAddress = ipAddress;
		m_serviceName = serviceName;
	}

	/**
	 * <p>getIpAddress</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Column(name="criticalPathIp")
	public String getIpAddress() {
		return m_ipAddress;
	}
	
	/**
	 * <p>setIpAddress</p>
	 *
	 * @param ipAddress a {@link java.lang.String} object.
	 */
	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}
	
	/**
	 * <p>getServiceName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Column(name="criticalPathServiceName")
	public String getServiceName() {
		return m_serviceName;
	}
	
	/**
	 * <p>setServiceName</p>
	 *
	 * @param serviceName a {@link java.lang.String} object.
	 */
	public void setServiceName(String serviceName) {
		m_serviceName = serviceName;
	}


}
