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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.jasper.helper;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * JasperReports scriptlet to retrieve start and end dates from predefined
 * ranges.
 * 
 * @author ronny
 */
public class TimeRange {

    /**
     * Implement last year, last month, this year, this month
     */
    private enum TIME_RANGE {
        LAST_YEAR, LAST_MONTH, THIS_YEAR, THIS_MONTH
    }

    /**
     * Today
     */
    private Calendar m_now;

    /**
     * Start date
     */
    private Calendar m_startDate;

    /**
     * End date
     */
    private Calendar m_endDate;

    /**
     * Constructor init now
     */
    public TimeRange() {
        this.m_now = GregorianCalendar.getInstance();
    }

    /**
     * <p>
     * getStartDate
     * </p>
     * 
     * @param range
     *            a {@link java.lang.String} object
     * @return a {@link java.sql.Timestamp} object
     */
    public Timestamp getStartDate(String range) {
        Timestamp ts = null;
        if (TIME_RANGE.LAST_YEAR.name().equalsIgnoreCase(range)) {
            this.m_startDate = new GregorianCalendar(
                                                     this.m_now.get(Calendar.YEAR) - 1,
                                                     0, 1);
            ts = new Timestamp(this.m_startDate.getTimeInMillis());
        } else if (TIME_RANGE.LAST_MONTH.name().equalsIgnoreCase(range)) {
            this.m_startDate = new GregorianCalendar(
                                                     this.m_now.get(Calendar.YEAR),
                                                     this.m_now.get(Calendar.MONTH) - 1,
                                                     1);
            ts = new Timestamp(this.m_startDate.getTimeInMillis());
        } else if (TIME_RANGE.THIS_YEAR.name().equalsIgnoreCase(range)) {
            this.m_startDate = new GregorianCalendar(
                                                     this.m_now.get(Calendar.YEAR),
                                                     0, 1);
            ts = new Timestamp(this.m_startDate.getTimeInMillis());
        } else if (TIME_RANGE.THIS_MONTH.name().equalsIgnoreCase(range)) {
            this.m_startDate = new GregorianCalendar(
                                                     this.m_now.get(Calendar.YEAR),
                                                     this.m_now.get(Calendar.MONTH),
                                                     1);
            ts = new Timestamp(this.m_startDate.getTimeInMillis());
        }
        return ts;
    }

    /**
     * <p>
     * getEndDate
     * </p>
     * 
     * @param range
     *            a {@link java.lang.String} object
     * @return a {@link java.sql.Timestamp} object
     */
    public Timestamp getEndDate(String range) {
        Timestamp ts = null;

        if (TIME_RANGE.LAST_YEAR.name().equalsIgnoreCase(range)) {
            this.m_endDate = new GregorianCalendar(
                                                   this.m_now.get(Calendar.YEAR) - 1,
                                                   12, 0, 23, 59, 59);
            ts = new Timestamp(this.m_endDate.getTimeInMillis());
        } else if (TIME_RANGE.LAST_MONTH.name().equalsIgnoreCase(range)) {
            this.m_endDate = new GregorianCalendar(
                                                   this.m_now.get(Calendar.YEAR),
                                                   this.m_now.get(Calendar.MONTH),
                                                   0, 23, 59, 59);
            ts = new Timestamp(this.m_endDate.getTimeInMillis());
        } else if (TIME_RANGE.THIS_YEAR.name().equalsIgnoreCase(range)) {
            this.m_endDate = this.m_now;
            ts = new Timestamp(this.m_endDate.getTimeInMillis());
        } else if (TIME_RANGE.THIS_MONTH.name().equalsIgnoreCase(range)) {
            this.m_endDate = this.m_now;
            ts = new Timestamp(this.m_endDate.getTimeInMillis());
        }

        return ts;
    }
}
