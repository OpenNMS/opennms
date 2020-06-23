/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
