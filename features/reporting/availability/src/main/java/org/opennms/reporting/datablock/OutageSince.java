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
     */
    public String getNodeName() {
        return m_nodename;
    }

    /**
     * Returns Downtime
     */
    public long getOutage() {
        return m_outage;
    }

    /**
     * Returns Down since
     */
    public long getOutTime() {
        return m_outTime;
    }

    /**
     * Returns the string format of this object
     */
    public String toString() {
        return " Node Name: " + m_nodename + " Out Time Since : " + new Date(m_outTime) + " Outage : " + m_outage;
    }
}
