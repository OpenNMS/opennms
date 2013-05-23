/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.reporting.datablock;

/**
 * This class holds the managed node/ip/service information for valid outage
 * nodes.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 */
public class IfService extends Object {

    /**
     * Nodeid
     */
    int nodeid;

    /**
     * Ipaddress
     */
    String ipaddr;

    /**
     * Serviceid
     */
    int serviceid;

    /**
     * Node Name
     */
    String nodeName;

    /**
     * Service Name
     */
    String serviceName;

    /**
     * Default Constructor.
     */
    public IfService() {
        nodeid = -1;
        ipaddr = null;
        serviceid = -1;
    }

    /**
     * Constructor that initialises the nodeid, ipaddr, service.
     *
     * @param node a int.
     * @param ip a {@link java.lang.String} object.
     * @param service a int.
     * @param name a {@link java.lang.String} object.
     * @param svcname a {@link java.lang.String} object.
     */
    public IfService(int node, String ip, int service, String name, String svcname) {
        nodeid = node;
        ipaddr = ip;
        serviceid = service;
        nodeName = name;
        serviceName = svcname;
    }

    /**
     * Sets the node id.
     *
     * @param id
     *            Node id to be set
     */
    public void setNodeID(int id) {
        nodeid = id;
    }

    /**
     * Return node name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * Sets the Service name.
     *
     * @param name
     *            Service name
     */
    public void setServiceName(String name) {
        serviceName = name;
    }

    /**
     * Return service name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the node name.
     *
     * @param name
     *            Node name to be set
     */
    public void setNodeName(String name) {
        nodeName = name;
    }

    /**
     * Returns the Node id
     *
     * @return node id.
     */
    public int getNodeID() {
        return nodeid;
    }

    /**
     * Sets the ipaddr.
     *
     * @param ip
     *            ipaddress to be set
     */
    public void setIpaddr(String ip) {
        ipaddr = ip;
    }

    /**
     * This method returns the ipaddress.
     *
     * @return Returns the ipaddress.
     */
    public String getIpaddr() {
        return ipaddr;
    }

    /**
     * This method sets the service.
     *
     * @param service
     *            Service id to be set.
     */
    public void setServiceId(int service) {
        serviceid = service;
    }

    /**
     * This returns the service id.
     *
     * @return a int.
     */
    public int getServiceId() {
        return serviceid;
    }

    /**
     * toString method
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("Node id " + nodeName + " Ipaddr " + ipaddr + " Service name " + serviceName);
    }
}
