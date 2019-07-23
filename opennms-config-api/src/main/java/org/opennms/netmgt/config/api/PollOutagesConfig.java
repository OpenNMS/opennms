/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.api;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.opennms.netmgt.config.poller.outages.Interface;
import org.opennms.netmgt.config.poller.outages.Node;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Time;

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
    boolean isNodeIdInOutage(long lnodeid, String outName);

    /**
     * Return if interfaces is part of specified outage.
     *
     * @param linterface
     *            the interface to be looked up
     * @param outName
     *            the outage name
     * @return the interface is part of the specified outage
     */
    boolean isInterfaceInOutage(String linterface, String outName);

    /**
     * Return if current time is part of specified outage.
     *
     * @param outName
     *            the outage name
     * @return true if current time is in outage
     */
    boolean isCurTimeInOutage(String outName);
    
    /**
     * Return if time is part of specified outage.
     *
     * @param time
     *            the time in millis to look up
     * @param outName
     *            the outage name
     * @return true if time is in outage
     */
    boolean isTimeInOutage(long time, String outName);

    /**
     * Return the specified outage. Null if there is no outage with the given name.
     */
    Outage getOutage(String outageCalendar);
    
    public String getOutageType(final String name);

    public List<Outage> getOutages();

    public Lock getReadLock();

    public List<Node> getNodeIds(final String name);

    public List<Interface> getInterfaces(final String name);

    public List<Time> getOutageTimes(final String name);

    public boolean isCurTimeInOutage(final Outage out);

    public boolean isNodeIdInOutage(final long lnodeid, final Outage out);

    public boolean isInterfaceInOutage(final String linterface, final Outage out);

    public void update();

    Calendar getEndOfOutage(String scheduledOutageName);

}
