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
// Created: March 16th, 2010 jonathan@opennms.org
//
// Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.api.reporting.parameter.ReportDateParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.netmgt.dao.castor.DefaultOnmsReportConfigDao;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DefaultParameterConversionServiceTest {
    
    private static DefaultOnmsReportConfigDao m_dao;
    private static DefaultParameterConversionService m_conversionService;
    
    private static final String ID = "defaultCalendarReport";
    
    @BeforeClass
    public static void setUp() throws Exception {
        Resource resource = new ClassPathResource("/opennms-reports-testdata.xml");
        m_dao = new DefaultOnmsReportConfigDao();
        m_dao.setConfigResource(resource);
        m_dao.afterPropertiesSet();
        m_conversionService = new DefaultParameterConversionService();
    }
    
    @Test
    public void testGetDateParms() {
        assertNotNull(m_dao.getDateParms(ID));
        assertEquals(1,m_dao.getDateParms(ID).length);
    }
    
    @Test
    public void testDefaultDateConversion() {
        ReportParameters parameters = m_conversionService.convert(m_dao.getParameters(ID));
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        System.out.println("test date " + cal.getTime().toString());
        cal.add(Calendar.DATE, -1);
        Date configDate = parameters.getDateParms().get(0).getValue();
        assertEquals(0, configDate.compareTo(cal.getTime()));
    }
    
    @Test
    public void testModifiedDateConversion() {
        ReportParameters parameters = m_conversionService.convert(m_dao.getParameters(ID));
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND,0);
        ReportDateParm dateParm = parameters.getDateParms().get(0);
        dateParm.setCount(1);
        dateParm.setInterval("month");
        dateParm.setHours(0);
        dateParm.setMinutes(0);
        Map <String, Object> parmMap = parameters.getReportParms();
        Date storedDate = (Date) parmMap.get("endDate");
        assertEquals(0, storedDate.compareTo(cal.getTime()));
    }
    

}
