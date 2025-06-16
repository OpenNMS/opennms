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

/**
 * <p>ServiceInfo class.</p>
 */
public class ServiceInfo {
    private String serviceStatus;
    private String serviceName;
    
    /**
     * <p>Constructor for ServiceInfo.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param status a {@link java.lang.String} object.
     */
    public ServiceInfo(String name, String status){
    	this.serviceStatus = status;
    	this.serviceName = name;
    }
	/**
	 * <p>Getter for the field <code>serviceName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getServiceName(){
		return serviceName;
	}
	/**
	 * <p>Getter for the field <code>serviceStatus</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getServiceStatus(){
		return serviceStatus;
	}
}
