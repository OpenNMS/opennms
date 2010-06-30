/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
    public long getInterval() {
        return interval;
    }

    /**
     * <p>scheduledSuspension</p>
     *
     * @return a boolean.
     */
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
