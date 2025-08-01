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
package org.opennms.netmgt.snmpinterfacepoller.pollable;

import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollContext
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollContext {
    
    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName();
    
    /**
     * <p>setServiceName</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public void setServiceName(String serviceName);
    
    /**
     * <p>sendEvent</p>
     *
     * @param event the event to send
     */
    public void sendEvent(Event event);

    /**
     * <p>createEvent</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param address a {@link java.lang.String} object.
     * @param netMask a {@link java.lang.String} object.
     * @param date a {@link java.util.Date} object.
     * @return the event
     * @param snmpinterface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public Event createEvent(String uei, int nodeId, String address, String netMask, Date date, OnmsSnmpInterface snmpinterface);
    
    /**
     * <p>get</p>
     *
     * @param nodeId a int.
     * @param criteria a {@link java.lang.String} object.
     * @return The List of OnmsSnmpInterfaces to be polled
     */
    public List<OnmsSnmpInterface> get(int nodeId, String criteria);

    /**
     * <p>getPollableNodesByIp</p>
     *
     * @param ipaddr the ip address of the node.
     * @return The List of OnmsIpInterfaces to be polled
     */
    public List<OnmsIpInterface> getPollableNodesByIp(String ipaddr);

    /**
     * <p>getPollableNodes</p>
     *
     * @return The List of OnmsIpInterfaces to be polled
     */
    public List<OnmsIpInterface> getPollableNodes();

    /**
     * Update the OnmsSnmpInterface
     *
     * @param snmpinteface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     */
    public void update(OnmsSnmpInterface snmpinteface);

    public String getLocation(Integer nodeId);

    public LocationAwareSnmpClient getLocationAwareSnmpClient();

}
