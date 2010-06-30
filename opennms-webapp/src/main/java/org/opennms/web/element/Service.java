//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.element;

/**
 * <p>Service class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
public class Service {
    private int m_id;
    
    private int m_nodeId;

    private int m_ifIndex;

    private String m_ipAddr;

    private int m_serviceId;

    private String m_serviceName;

    private String m_lastGood;

    private String m_lastFail;

    private String m_notify;

    private char m_status;

    /**
     * <p>Constructor for Service.</p>
     */
    public Service() {
    }

    /**
     * <p>Constructor for Service.</p>
     *
     * @param id a int.
     * @param nodeid a int.
     * @param ifindex a int.
     * @param ipaddr a {@link java.lang.String} object.
     * @param serviceid a int.
     * @param serviceName a {@link java.lang.String} object.
     * @param lastGood a {@link java.lang.String} object.
     * @param lastFail a {@link java.lang.String} object.
     * @param notify a {@link java.lang.String} object.
     * @param status a char.
     */
    public Service(int id, int nodeid, int ifindex, String ipaddr, int serviceid, String serviceName, String lastGood, String lastFail, String notify, char status) {
        setId(id);
        setNodeId(nodeid);
        setIfIndex(ifindex);
        setIpAddress(ipaddr);
        setServiceId(serviceid);
        setServiceName(serviceName);
        setLastGood(lastGood);
        setLastFail(lastFail);
        setNotify(notify);
        setStatus(status);
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
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    public int getIfIndex() {
        return m_ifIndex;
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
        str.append("Ifindex = " + getIfIndex() + "\n");
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

    void setIfIndex(int ifIndex) {
        m_ifIndex = ifIndex;
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
