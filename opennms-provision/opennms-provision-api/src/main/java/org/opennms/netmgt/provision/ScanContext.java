/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision;

import java.net.InetAddress;

public interface ScanContext {

    /**
     * Return the preferred address used to talk to the agent of type type provided
     * 
     * e.g.  use getAgentAddress("SNMP") to find the InetAddress for the SNMP Agent for the node being scanned.
     * 
     * @param agentType the type of agent to search for
     * @return the InetAddress for the agent or null if no such agent exists
     */
    public InetAddress getAgentAddress(String agentType);
    
    public void updateSysObjectId(String sysObjectId);
    public void updateSysName(String sysName);
    public void updateSysDescription(String sysDescription);
    public void updateSysLocation(String sysLocation);
    public void updateSysContact(String sysContact);

}