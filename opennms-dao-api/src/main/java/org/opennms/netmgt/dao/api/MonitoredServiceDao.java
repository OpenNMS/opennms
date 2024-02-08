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
package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ServiceSelector;

/**
 * <p>MonitoredServiceDao interface.</p>
 *
 * @author Craig Gallen
 * @author David Hustace
 */
public interface MonitoredServiceDao extends LegacyOnmsDao<OnmsMonitoredService, Integer> {

    /**
     * <p>get</p>
     * 
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddress a {@link java.net.InetAddress} object.
     * @param serviceId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    OnmsMonitoredService get(Integer nodeId, InetAddress ipAddress, Integer serviceId);

    /**
     * <p>get</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddr a {@link java.net.InetAddress} object.
     * @param ifIndex a {@link java.lang.Integer} object.
     * @param serviceId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    OnmsMonitoredService get(Integer nodeId, InetAddress ipAddr, Integer ifIndex, Integer serviceId);

    /**
     * <p>get</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param ipAddress a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    OnmsMonitoredService get(Integer nodeId, InetAddress ipAddress, String svcName);

    /**
     * <p>findByType</p>
     *
     * @param typeName a {@link java.lang.String} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsMonitoredService> findByType(String typeName);

    /**
     * <p>findMatchingServices</p>
     *
     * @param serviceSelector a {@link org.opennms.netmgt.model.ServiceSelector} object.
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsMonitoredService> findMatchingServices(ServiceSelector serviceSelector);

    /**
     * <p>findAllServices</p>
     *
     * Use HQL to get OnmsMonitoredService
     * joined with IpInterface and Node
     * No lazy initialization 
     * 
     * @return a {@link java.util.Collection} object.
     */
    List<OnmsMonitoredService> findAllServices();

    /**
     * <p>findByApplication</p>
     *
     * @param application a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link java.util.Collection} object.
     */
    Set<OnmsMonitoredService> findByApplication(OnmsApplication application);
    
    /**
     * <p>getPrimaryService</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    OnmsMonitoredService getPrimaryService(Integer nodeId, String svcName);

    List<OnmsMonitoredService> findByNode(final int nodeId);
}
