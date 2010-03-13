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
// Created: January 27th, 2010 jonathan@opennms.org
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
package org.opennms.reporting.core.svclayer.support;

import java.util.ArrayList;
import java.util.Calendar;

import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.netmgt.config.reporting.DateParm;
import org.opennms.netmgt.config.reporting.IntParm;
import org.opennms.netmgt.config.reporting.StringParm;
import org.opennms.netmgt.config.reporting.Parameters;
import org.opennms.reporting.core.svclayer.ParameterConversionService;

public class DefaultParameterConversionService implements
        ParameterConversionService {

    public ReportParameters convert(Parameters configParameters) {

        ReportParameters reportParameters = new ReportParameters();
        
        
        // add date parms to criteria
        
        ArrayList<ReportDateParm> dateParms = new ArrayList<ReportDateParm>();
        DateParm[] dates = configParameters.getDateParm();
        if (dates.length > 0) {
            for (int i = 0 ; i < dates.length ; i++ ) {
                ReportDateParm dateParm = new ReportDateParm();
                dateParm.setUseAbsoluteDate(dates[i].getUseAbsoluteDate());
                dateParm.setDisplayName(dates[i].getDisplayName());
                dateParm.setName(dates[i].getName());
                dateParm.setCount(new Integer((int) dates[i].getDefaultCount()));
                dateParm.setInterval(dates[i].getDefaultInterval());
                Calendar cal = Calendar.getInstance();
                if (dates[i].getDefaultTime() != null) {
                    dateParm.setHours(dates[i].getDefaultTime().getHours());
                    cal.set(Calendar.HOUR, dateParm.getHours());
                    dateParm.setMinutes(dates[i].getDefaultTime().getMinutes());
                    cal.set(Calendar.MINUTE, dateParm.getMinutes());
                }
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
        reportParameters.setDateParms(dateParms);
        
        // add string parms to criteria
        
        ArrayList<ReportStringParm> stringParms = new ArrayList<ReportStringParm>();
        StringParm[] strings = configParameters.getStringParm();
        if (strings.length > 0) {
            for (int i = 0 ; i < strings.length ; i++ ) {
                ReportStringParm stringParm = new ReportStringParm();
                stringParm.setDisplayName(strings[i].getDisplayName());
                stringParm.setName(strings[i].getName());
                stringParm.setInputType(strings[i].getInputType());
                stringParm.setValue(strings[i].getDefault());
                stringParms.add(stringParm);
            }
        }
        reportParameters.setStringParms(stringParms);
        
        // add int parms to criteria
        
        ArrayList<ReportIntParm> intParms = new ArrayList<ReportIntParm>();
        IntParm[] integers = configParameters.getIntParm();
        if (integers.length > 0) {
            for (int i = 0 ; i < integers.length ; i++ ) {
                ReportIntParm intParm = new ReportIntParm();
                intParm.setDisplayName(integers[i].getDisplayName());
                intParm.setName(integers[i].getName());
                intParm.setInputType(integers[i].getInputType());
                intParm.setValue(integers[i].getDefault());
                intParms.add(intParm);
            }
        }
        reportParameters.setIntParms(intParms);

        return reportParameters;
    }

}
