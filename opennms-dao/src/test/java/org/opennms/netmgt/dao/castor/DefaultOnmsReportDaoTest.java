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
package org.opennms.netmgt.dao.castor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.config.reporting.DateParm;
import org.opennms.netmgt.config.reporting.IntParm;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class DefaultOnmsReportDaoTest {
    
    private static final String ID = "defaultCalendarReport";
    private static final String ALTERNATE_ID = "defaultClassicReport";
    private static final String TYPE = "calendar";
    private static final String SVG_TEMPLATE = "SVGAvailReport.xsl";
    private static final String PDF_TEMPLATE = "PDFAvailReport.xsl";
    private static final String HTML_TEMPLATE = "HTMLAvailReport.xsl";
    private static final String LOGO = "logo.gif";
    private static final String DATE_DISPLAY_NAME = "end date";
    private static final String DATE_NAME = "endDate";
    private static final String STRING_NAME = "offenderCount";
    private static final String STRING_DISPLAY_NAME = "top offender count";
    private static DefaultOnmsReportConfigDao m_dao;
    

    @BeforeClass
    public static void setUp() throws Exception {
        Resource resource = new ClassPathResource("/opennms-reports-testdata.xml");
        m_dao = new DefaultOnmsReportConfigDao();
        m_dao.setConfigResource(resource);
        m_dao.afterPropertiesSet();
    }
    
    @Test
    public void testGetRenderParms() throws Exception {

        assertEquals(m_dao.getType(ID),TYPE);
        assertEquals(m_dao.getSvgStylesheetLocation(ID), SVG_TEMPLATE);
        assertEquals(m_dao.getPdfStylesheetLocation(ID), PDF_TEMPLATE);
        assertEquals(m_dao.getHtmlStylesheetLocation(ID), HTML_TEMPLATE);
        assertEquals(m_dao.getLogo(ID), LOGO);
        // test to see if missing parameters return null
        assertNull(m_dao.getSvgStylesheetLocation(ALTERNATE_ID));
    }
    
    @Test
    public void testGetReportParms() throws Exception {
        
        DateParm[] dates = m_dao.getDateParms(ID);
        assertEquals(1, dates.length);
        assertEquals(DATE_NAME,dates[0].getName());
        assertEquals(DATE_DISPLAY_NAME,dates[0].getDisplayName());
        assertEquals(false,dates[0].getUseAbsoluteDate());
        assertEquals(1,dates[0].getDefaultCount());
        assertEquals("day",dates[0].getDefaultInterval());
        assertEquals(23,dates[0].getDefaultTime().getHours());
        assertEquals(59,dates[0].getDefaultTime().getMinutes());
        
        IntParm[] integers = m_dao.getIntParms(ID);
        assertEquals(1,integers.length);
        assertEquals(STRING_NAME,integers[0].getName());
        assertEquals(STRING_DISPLAY_NAME,integers[0].getDisplayName());
        assertEquals(20,integers[0].getDefault());
        
    }

}
