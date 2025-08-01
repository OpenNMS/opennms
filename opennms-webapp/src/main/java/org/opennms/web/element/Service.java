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
package org.opennms.web.element;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class Service {
    private int m_id;
    
    private int m_nodeId;

    private String m_ipAddr;

    private int m_serviceId;

    private String m_serviceName;

    private String m_lastGood;

    private String m_lastFail;

    private String m_notify;

    private char m_status;

    Service(OnmsMonitoredService monSvc) {
        setId(monSvc.getId());
        setNodeId(monSvc.getNodeId());
        
        setIpAddress(InetAddressUtils.str(monSvc.getIpAddress()));
        setServiceId(monSvc.getServiceId());
        setServiceName(monSvc.getServiceName());
        if(monSvc.getLastGood() != null) {
           setLastGood(StringUtils.toStringEfficiently(monSvc.getLastGood()));
        }
        if(monSvc.getLastFail() != null) {
            setLastFail(StringUtils.toStringEfficiently(monSvc.getLastFail()));
        }
        setNotify(monSvc.getNotify());
        if(monSvc.getStatus() != null) {
            setStatus(monSvc.getStatus().charAt(0));
        }

    }
    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getId() {
        return m_id;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * <p>getIpAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddress() {
        return m_ipAddr;
    }

    /**
     * <p>getServiceId</p>
     *
     * @return a int.
     */
    public int getServiceId() {
        return m_serviceId;
    }

    /**
     * <p>getServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_serviceName;
    }

    /**
     * <p>getLastGood</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLastGood() {
        return m_lastGood;
    }

    /**
     * <p>getLastFail</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLastFail() {
        return m_lastFail;
    }

    /**
     * <p>getNotify</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNotify() {
        return m_notify;
    }

    /**
     * <p>getStatus</p>
     *
     * @return a char.
     */
    public char getStatus() {
        return m_status;
    }

    /**
     * <p>isManaged</p>
     *
     * @return a boolean.
     */
    public boolean isManaged() {
        return (getStatus() == 'A');
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder str = new StringBuilder("Node Id = " + getNodeId() + "\n");
        str.append("Ipaddr = " + getIpAddress() + "\n");
        str.append("Service id = " + getServiceId() + "\n");
        str.append("Service name = " + getServiceName() + "\n");
        str.append("Last Good = " + getLastGood() + "\n");
        str.append("Last Fail  = " + getLastFail() + "\n");
        str.append("Status = " + getStatus() + "\n");
        return str.toString();
    }

    void setId(int id) {
        m_id = id;
    }

    void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }

    void setIpAddress(String ipAddr) {
        m_ipAddr = ipAddr;
    }

    void setServiceId(int serviceId) {
        m_serviceId = serviceId;
    }

    void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }

    void setLastGood(String lastGood) {
        m_lastGood = lastGood;
    }

    void setLastFail(String lastFail) {
        m_lastFail = lastFail;
    }

    void setNotify(String notify) {
        m_notify = notify;
    }

    void setStatus(char status) {
        m_status = status;
    }
}
