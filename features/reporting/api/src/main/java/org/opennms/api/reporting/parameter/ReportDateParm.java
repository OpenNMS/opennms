//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
// 
// Created: October 5th, 2009 jonathan@opennms.org
//
// Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.api.reporting.parameter;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.opennms.api.reporting.ReportMode;

/**
 * <p>ReportDateParm class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ReportDateParm extends ReportParm implements Serializable {

    private static final long serialVersionUID = -8528562178984136887L;
    
    private Date m_date;
    private Boolean m_useAbsoluteDate;
    private String m_interval;
    private Integer m_count;
    private Integer m_hours;
    private Integer m_minutes;
    
    /**
     * <p>Constructor for ReportDateParm.</p>
     */
    public ReportDateParm() {
        super();
    }
    
    /**
     * <p>getUseAbsoluteDate</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getUseAbsoluteDate() {
        return m_useAbsoluteDate;
    }

    /**
     * <p>setUseAbsoluteDate</p>
     *
     * @param useAbsoluteDate a {@link java.lang.Boolean} object.
     */
    public void setUseAbsoluteDate(Boolean useAbsoluteDate) {
        m_useAbsoluteDate = useAbsoluteDate;
    }

    /**
     * <p>getInterval</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInterval() {
        return m_interval;
    }

    /**
     * <p>setInterval</p>
     *
     * @param interval a {@link java.lang.String} object.
     */
    public void setInterval(String interval) {
        m_interval = interval;
    }

    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getCount() {
        return m_count;
    }

    /**
     * <p>setCount</p>
     *
     * @param count a {@link java.lang.Integer} object.
     */
    public void setCount(Integer count) {
        m_count = count;
    }
    
    /**
     * <p>getDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getDate() {
        return m_date;
    }
    /**
     * <p>setDate</p>
     *
     * @param date a {@link java.util.Date} object.
     */
    public void setDate(Date date) {
        m_date = date;
    }
       
    public Date getValue(ReportMode mode) {
        Calendar cal = Calendar.getInstance();
        if ((mode == ReportMode.SCHEDULED) && (m_useAbsoluteDate == false)) {
            // use the offset date set when the report was scheduled
            int amount = 0 - m_count;
            if (m_interval.equals("year")) {
                cal.add(Calendar.YEAR, amount);
            } else { 
                if (m_interval.equals("month")) {
                    cal.add(Calendar.MONTH, amount);
                } else {
                    cal.add(Calendar.DATE, amount);
                }
            }
        } else {
            cal.setTime(m_date);
        } 
        if (m_hours != null) {
            cal.set(Calendar.HOUR_OF_DAY, m_hours);
        } else {
            cal.set(Calendar.HOUR_OF_DAY, 0);
        }
        if (m_minutes != null) {
            cal.set(Calendar.MINUTE, m_minutes);
        } else {
            cal.set(Calendar.MINUTE, 0);
        }                
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        return cal.getTime();
    }

    /**
     * <p>getHours</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getHours() {
        return m_hours;
    }

    /**
     * <p>setHours</p>
     *
     * @param hour a {@link java.lang.Integer} object.
     */
    public void setHours(Integer hour) {
        m_hours = hour;
    }

    /**
     * <p>getMinutes</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getMinutes() {
        return m_minutes;
    }

    /**
     * <p>setMinutes</p>
     *
     * @param minute a {@link java.lang.Integer} object.
     */
    public void setMinutes(Integer minute) {
        m_minutes = minute;
    } 

}
