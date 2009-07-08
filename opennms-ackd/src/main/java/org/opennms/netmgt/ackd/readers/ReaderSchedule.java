/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ackd.readers;

import java.util.concurrent.TimeUnit;

class ReaderSchedule {
    
    private long m_initialDelay;
    private long m_interval;
    private long m_attemptsRemaining;
    private TimeUnit m_unit;
    
    public static ReaderSchedule createSchedule() {
        return new ReaderSchedule();
    }

    public static ReaderSchedule createSchedule(long initDelay, long interval, int attempts, TimeUnit unit) {
        return new ReaderSchedule(initDelay, interval, attempts, unit);
    }

    private ReaderSchedule() {
        this(60, 60, 1, TimeUnit.SECONDS);
    }
    
    private ReaderSchedule(long initDelay, long interval, int attempts, TimeUnit unit) {
        m_initialDelay = initDelay;
        m_interval = interval;
        m_attemptsRemaining = attempts;
        m_unit = unit;
    }

    public long getInitialDelay() {
        return m_initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        m_initialDelay = initialDelay;
    }

    public long getInterval() {
        return m_interval;
    }

    public void setInterval(long interval) {
        m_interval = interval;
    }

    public long getAttemptsRemaining() {
        return m_attemptsRemaining;
    }

    public void setAttemptsRemaining(long attemptsRemaining) {
        m_attemptsRemaining = attemptsRemaining;
    }

    public TimeUnit getUnit() {
        return m_unit;
    }

    public void setUnit(TimeUnit unit) {
        m_unit = unit;
    }
    
}
