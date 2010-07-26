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
// Tab Size = 8
//

package org.opennms.reporting.datablock;

/**
 * This class holds the managed node/ip/service information for valid outage
 * nodes.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 * @author <A HREF="http://www.oculan.com">oculan.com </A>
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 * @author <A HREF="http://www.oculan.com">oculan.com </A>
 * @version $Id: $
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
    public String toString() {
        return ("Node id " + nodeName + " Ipaddr " + ipaddr + " Service name " + serviceName);
    }
}
