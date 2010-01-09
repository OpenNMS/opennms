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
// Created: October 5th, 2009
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
package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Calendar;

import org.opennms.netmgt.config.databaseReports.DateParm;
import org.opennms.netmgt.config.databaseReports.IntParm;
import org.opennms.netmgt.config.databaseReports.StringParm;
import org.opennms.netmgt.dao.DatabaseReportConfigDao;
import org.opennms.reporting.core.model.DatabaseReportDateParm;
import org.opennms.reporting.core.model.DatabaseReportIntParm;
import org.opennms.reporting.core.model.DatabaseReportStringParm;
import org.opennms.web.command.DatabaseReportCriteriaCommand;
import org.opennms.web.svclayer.DatabaseReportCriteriaService;

public class DefaultDatabaseReportCriteriaService implements
        DatabaseReportCriteriaService {
    
    DatabaseReportConfigDao m_dao;

    public DatabaseReportCriteriaCommand getCriteria(String id) {
       
        DatabaseReportCriteriaCommand command = new DatabaseReportCriteriaCommand();
        
        command.setReportId(id);
        command.setDisplayName(m_dao.getDisplayName(id));
        
        // add date parms to criteria
        
        ArrayList<DatabaseReportDateParm> dateParms = new ArrayList<DatabaseReportDateParm>();
        DateParm[] dates = m_dao.getDateParms(id);
        if (dates.length > 0) {
            for (int i = 0 ; i < dates.length ; i++ ) {
                DatabaseReportDateParm dateParm = new DatabaseReportDateParm();
                dateParm.setUseAbsoluteDate(dates[i].getUseAbsoluteDate());
                dateParm.setDisplayName(dates[i].getDisplayName());
                dateParm.setName(dates[i].getName());
                dateParm.setCount(new Integer((int) dates[i].getDefaultCount()));
                dateParm.setInterval(dates[i].getDefaultInterval());
                Calendar cal = Calendar.getInstance();
                int amount = 0 - dates[i].getDefaultCount();
                if (dates[i].getDefaultInterval().equals("year")) {
                    cal.add(Calendar.YEAR, amount);
                } else { 
                    if (dates[i].getDefaultInterval().equals("month")) {
                        cal.add(Calendar.MONTH, amount);
                    } else {
                        cal.add(Calendar.DATE, amount);
                    }
                }   
                dateParm.setValue(cal.getTime());
                dateParms.add(dateParm);
            }
        }
        command.setDateParms(dateParms);
        
        // add string parms to criteria
        
        ArrayList<DatabaseReportStringParm> stringParms = new ArrayList<DatabaseReportStringParm>();
        StringParm[] strings = m_dao.getStringParms(id);
        if (strings.length > 0) {
            for (int i = 0 ; i < strings.length ; i++ ) {
                DatabaseReportStringParm stringParm = new DatabaseReportStringParm();
                stringParm.setDisplayName(strings[i].getDisplayName());
                stringParm.setName(strings[i].getName());
                stringParm.setInputType(strings[i].getInputType());
                stringParm.setValue(strings[i].getDefault());
                stringParms.add(stringParm);
            }
        }
        command.setStringParms(stringParms);
        
        // add int parms to criteria
        
        ArrayList<DatabaseReportIntParm> intParms = new ArrayList<DatabaseReportIntParm>();
        IntParm[] integers = m_dao.getIntParms(id);
        if (integers.length > 0) {
            for (int i = 0 ; i < integers.length ; i++ ) {
                DatabaseReportIntParm intParm = new DatabaseReportIntParm();
                intParm.setDisplayName(integers[i].getDisplayName());
                intParm.setName(integers[i].getName());
                intParm.setInputType(integers[i].getInputType());
                intParm.setValue(integers[i].getDefault());
                intParms.add(intParm);
            }
        }
        command.setIntParms(intParms);

        return command;
    }
    
    public void setDatabaseReportConfigDao(DatabaseReportConfigDao databaseReportDao) {
        m_dao = databaseReportDao;
    }

}
