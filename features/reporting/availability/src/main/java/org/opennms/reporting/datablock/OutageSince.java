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

import java.util.Date;

/**
 * <B>OutageSince </B> holds the temporary storage for data used during
 * availability report generation presented in the report.
 */
public class OutageSince {
    /**
     * Node name
     */
    private String m_nodename;

    /**
     * Down since
     */
    private long m_outTime;

    /**
     * DownTime
     */
    private long m_outage;

    /**
     * Constructor
     *
     * @param nodename
     *            Node Name
     * @param outTime
     *            Start of Outage
     * @param outage
     *            Downtime
     */
    public OutageSince(String nodename, long outTime, long outage) {
        m_nodename = nodename;
        m_outTime = outTime;
        m_outage = outage;
    }

    /**
     * Returns Node name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeName() {
        return m_nodename;
    }

    /**
     * Returns Downtime
     *
     * @return a long.
     */
    public long getOutage() {
        return m_outage;
    }

    /**
     * Returns Down since
     *
     * @return a long.
     */
    public long getOutTime() {
        return m_outTime;
    }

    /**
     * Returns the string format of this object
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return " Node Name: " + m_nodename + " Out Time Since : " + new Date(m_outTime) + " Outage : " + m_outage;
    }
}
