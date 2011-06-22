/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;


/**
 * <p>PollOutagesConfig interface.</p>
 *
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public interface PollOutagesConfig {
    
    /**
     * Return if the node represented by the nodeid is part of specified outage.
     *
     * @param lnodeid
     *            the nodeid to be checked
     * @param outName
     *            the outage name
     * @return the node is part of the specified outage
     */
    public abstract boolean isNodeIdInOutage(long lnodeid, String outName);

    /**
     * Return if interfaces is part of specified outage.
     *
     * @param linterface
     *            the interface to be looked up
     * @param outName
     *            the outage name
     * @return the interface is part of the specified outage
     */
    public abstract boolean isInterfaceInOutage(String linterface, String outName);

    /**
     * Return if current time is part of specified outage.
     *
     * @param outName
     *            the outage name
     * @return true if current time is in outage
     */
    public abstract boolean isCurTimeInOutage(String outName);
    
    /**
     * Return if time is part of specified outage.
     *
     * @param time
     *            the time in millis to look up
     * @param outName
     *            the outage name
     * @return true if time is in outage
     */
    public abstract boolean isTimeInOutage(long time, String outName);

    /**
     * <p>update</p>
     *
     * @throws java.lang.Exception if any.
     */
    public abstract void update() throws Exception;
    

}
