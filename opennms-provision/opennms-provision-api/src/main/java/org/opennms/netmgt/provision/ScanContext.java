/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

import java.net.InetAddress;

/**
 * <p>ScanContext interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
    
    /**
     * <p>updateSysObjectId</p>
     *
     * @param sysObjectId a {@link java.lang.String} object.
     */
    public void updateSysObjectId(String sysObjectId);
    /**
     * <p>updateSysName</p>
     *
     * @param sysName a {@link java.lang.String} object.
     */
    public void updateSysName(String sysName);
    /**
     * <p>updateSysDescription</p>
     *
     * @param sysDescription a {@link java.lang.String} object.
     */
    public void updateSysDescription(String sysDescription);
    /**
     * <p>updateSysLocation</p>
     *
     * @param sysLocation a {@link java.lang.String} object.
     */
    public void updateSysLocation(String sysLocation);
    /**
     * <p>updateSysContact</p>
     *
     * @param sysContact a {@link java.lang.String} object.
     */
    public void updateSysContact(String sysContact);

}
