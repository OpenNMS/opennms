/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.features.reporting.dao.jasper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.reporting.model.jasperreport.SimpleJasperReportDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleJasperReportsDaoTest {

    Logger logger = LoggerFactory.getLogger(SimpleJasperReportsDaoTest.class);
    
    private SimpleJasperReportsDao m_dao;

    @Before
    public void setup() {
        System.setProperty("opennms.home", "src/test/resources");
        assertTrue(new File(System.getProperty("opennms.home")
                + File.separator + "etc" + File.separator
                + "simple-jasper-reports.xml").canRead());
    }

    @Test
    public void getValuesForSampleReportTest() {
        this.m_dao = new SimpleJasperReportsDao();
        assertEquals(2, m_dao.getReports().size());
        
        SimpleJasperReportDefinition report = (SimpleJasperReportDefinition) m_dao.getReports().get(0);
        
        assertEquals("423", report.getId());
        assertEquals("sample display-name", report.getDisplayName());
        assertEquals("file:///tmp/resource-uri-test.jrxml", report.getTemplate());

        report = (SimpleJasperReportDefinition) m_dao.getReports().get(1);
        assertEquals("23", report.getId());
        assertEquals("more sample display-name", report.getDisplayName());
        assertEquals("file:///tmp/resource-uri-test.jrxml", report.getTemplate());
 
    }
}
