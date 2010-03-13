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
// Created: November 9th, 2009 jonathan@opennms.org
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.opennms.api.reporting.ReportFormat;


public class ReportParameters implements Serializable {

    private static final long serialVersionUID = -3848794546173077375L;
    protected String m_reportId;
    protected ReportFormat m_format;
    protected String m_displayName;
    protected List <ReportDateParm> m_dateParms;
    protected List <ReportStringParm> m_stringParms;
    protected List <ReportIntParm> m_intParms;

    public ReportParameters() {
        super();
    }

    public List<ReportDateParm> getDateParms() {
        return m_dateParms;
    }

    public void setDateParms(List<ReportDateParm> dateParms) {
        m_dateParms = dateParms;
    }
    
    public List<ReportStringParm> getStringParms() {
        return m_stringParms;
    }

    public void setStringParms(List<ReportStringParm> strings) {
        m_stringParms = strings;
    }
    
    public List<ReportIntParm> getIntParms() {
        return m_intParms;
    }

    public void setIntParms(List<ReportIntParm> ints) {
        m_intParms = ints;
    }

    public void setReportId(String reportId) {
        m_reportId = reportId;
    }

    public String getReportId() {
        return m_reportId;
    }

    public void setDisplayName(String displayName) {
        m_displayName = displayName;
    }

    public String getDisplayName() {
        return m_displayName;
    }
    
    public void setFormat(ReportFormat format) {
        m_format = format;
    }

    public ReportFormat getFormat() {
        return m_format;
    }

    public HashMap<String, Object> getReportParms() {
        
        HashMap <String,Object>parmMap = new HashMap<String, Object>();
        
        // Add all the strings from the report
        if (m_stringParms != null ) {
            Iterator<ReportStringParm>stringIter = m_stringParms.iterator();
            while (stringIter.hasNext()) {
                ReportStringParm parm = stringIter.next();
                parmMap.put(parm.getName(), parm.getValue());
            }
        }
        // Add all the dates from the report
        if (m_dateParms != null) {
            Iterator<ReportDateParm>dateIter = m_dateParms.iterator();
            while (dateIter.hasNext()) {
                ReportDateParm parm = dateIter.next();
                Calendar cal = Calendar.getInstance();
                if (parm.getUseAbsoluteDate()) {
                    // use the absolute date set when the report was scheduled.
                    cal.setTime(parm.getValue());
                } else {
                    // use the offset date set when the report was scheduled
                    int amount = 0 - parm.getCount();
                    if (parm.getInterval().equals("year")) {
                        cal.add(Calendar.YEAR, amount);
                    } else { 
                        if (parm.getInterval().equals("month")) {
                            cal.add(Calendar.MONTH, amount);
                        } else {
                            cal.add(Calendar.DATE, amount);
                        }
                    }
                }
                cal.set(Calendar.HOUR_OF_DAY, parm.getHours());
                cal.set(Calendar.MINUTE, parm.getMinutes());              
                parmMap.put(parm.getName(), cal.getTime());
            }
        }
        
        // Add all the integers from the report
        if (m_intParms != null) {
            Iterator<ReportIntParm>intIter = m_intParms.iterator();
            while (intIter.hasNext()) {
                ReportIntParm parm = intIter.next();
                parmMap.put(parm.getName(), parm.getValue());
            }
        }
        
        return parmMap;
    }

}