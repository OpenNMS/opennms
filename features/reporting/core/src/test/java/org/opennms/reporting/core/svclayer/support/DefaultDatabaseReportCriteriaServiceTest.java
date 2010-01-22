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
package org.opennms.reporting.core.svclayer.support;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.castor.DefaultDatabaseReportConfigDao;
import org.opennms.reporting.core.model.DatabaseReportCriteria;
import org.opennms.reporting.core.svclayer.DatabaseReportCriteriaService;
import org.opennms.reporting.core.svclayer.support.DefaultDatabaseReportCriteriaService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DefaultDatabaseReportCriteriaServiceTest {
    
    private static final String ID = "defaultCalendarReport";
    private static final String DESCRIPTION = "default calendar report";
    private static final String DATE_DISPLAY_NAME = "end date";
    private static final String DATE_NAME = "endDate";
    private static final String STRING_NAME = "reportCategory";
    private static final String STRING_DISPLAY_NAME =  "report category";
    private static final String STRING_INPUT_TYPE = "reportCategorySelector";
    private static final String INT_NAME = "offenderCount";
    private static final String INT_DISPLAY_NAME =  "top offender count";
    private static final int INT_VALUE = 20;
    private static final Integer OFFSET_COUNT = 1;
    private static final String OFFSET_INTERVAL = "day";
    private static final String INT_INPUT_TYPE = "freeText";
    
    
    
    private DefaultDatabaseReportConfigDao m_dao;
    private DatabaseReportCriteriaService m_criteriaService;
    
    @Before
    public void setupDao() throws Exception {

        m_dao = new DefaultDatabaseReportConfigDao();
        Resource resource = new ClassPathResource("database-reports-testdata.xml");
        m_dao.setConfigResource(resource);
        m_dao.afterPropertiesSet();
        
        m_criteriaService = new DefaultDatabaseReportCriteriaService();
        m_criteriaService.setDatabaseReportConfigDao(m_dao);
        
    }
    
    
    @Test
    public void testDatabaseReportService() throws Exception {
        
        DatabaseReportCriteria criteria = m_criteriaService.getCriteria(ID);
        
        assertEquals(criteria.getStringParms().size(),1);
        assertEquals(criteria.getStringParms().get(0).getDisplayName(),STRING_DISPLAY_NAME);
        assertEquals(criteria.getStringParms().get(0).getName(),STRING_NAME);
        assertEquals(criteria.getStringParms().get(0).getInputType(),STRING_INPUT_TYPE);
        
        assertEquals(criteria.getDateParms().size(),1);
        assertEquals(criteria.getDateParms().get(0).getUseAbsoluteDate(),false);
        assertEquals(criteria.getDateParms().get(0).getDisplayName(),DATE_DISPLAY_NAME);
        assertEquals(criteria.getDateParms().get(0).getName(),DATE_NAME);
        
        assertEquals(criteria.getIntParms().size(),1);
        assertEquals(criteria.getIntParms().get(0).getName(),INT_NAME);
        assertEquals(criteria.getIntParms().get(0).getDisplayName(),INT_DISPLAY_NAME);
        assertEquals(criteria.getIntParms().get(0).getInputType(),INT_INPUT_TYPE);
        assertEquals(criteria.getIntParms().get(0).getValue(),INT_VALUE); 
        
    }
    
    @Test
    public void testCalendarOffset() {
        
        DatabaseReportCriteria criteria = m_criteriaService.getCriteria(ID);
        
        assertEquals(OFFSET_COUNT,criteria.getDateParms().get(0).getCount());
        assertEquals(OFFSET_INTERVAL,criteria.getDateParms().get(0).getInterval());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 0 - OFFSET_COUNT);
        Calendar configCal = Calendar.getInstance();
        configCal.setTime(criteria.getDateParms().get(0).getValue());
        assertEquals(cal.get(Calendar.DATE),configCal.get(Calendar.DATE));
        assertEquals(cal.get(Calendar.MONTH),configCal.get(Calendar.MONTH));
        assertEquals(cal.get(Calendar.YEAR),configCal.get(Calendar.YEAR));
        
        
    }

}
