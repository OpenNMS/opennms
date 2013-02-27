/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.element;

import org.opennms.core.utils.InetAddressUtils;
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
           setLastGood(monSvc.getLastGood().toString());
        }
        if(monSvc.getLastFail() != null) {
            setLastFail(monSvc.getLastFail().toString());
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
    public String toString() {
        StringBuffer str = new StringBuffer("Node Id = " + getNodeId() + "\n");
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
