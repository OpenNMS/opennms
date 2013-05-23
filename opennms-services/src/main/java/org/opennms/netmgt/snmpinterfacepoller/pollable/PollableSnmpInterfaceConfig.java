/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import org.opennms.netmgt.scheduler.ScheduleInterval;
import org.opennms.netmgt.scheduler.Timer;

/**
 * Represents a PollableSnmpInterfaceConfig
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class PollableSnmpInterfaceConfig implements ScheduleInterval {

    private Timer m_timer;
    private long interval;
    
    /**
     * <p>Getter for the field <code>interval</code>.</p>
     *
     * @return a long.
     */
    @Override
    public long getInterval() {
        return interval;
    }

    /**
     * <p>scheduledSuspension</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean scheduledSuspension() {
        return false;
    }

    /**
     * <p>getCurrentTime</p>
     *
     * @return a long.
     */
    public long getCurrentTime() {
        return m_timer.getCurrentTime();
    }

    /**
     * <p>Constructor for PollableSnmpInterfaceConfig.</p>
     *
     * @param timer a {@link org.opennms.netmgt.scheduler.Timer} object.
     * @param interval a long.
     */
    public PollableSnmpInterfaceConfig(Timer timer, long interval) {
        super();
        m_timer = timer;
        this.interval = interval;
    }

}
