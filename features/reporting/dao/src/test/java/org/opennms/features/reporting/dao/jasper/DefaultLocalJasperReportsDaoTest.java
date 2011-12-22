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
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLocalJasperReportsDaoTest {

    Logger logger = LoggerFactory.getLogger(DefaultLocalJasperReportsDaoTest.class);
    
    private LegacyLocalJasperReportsDao m_defaultLocalJasperReportsDao;
    
    @Before
    public void setup() {
        System.setProperty("opennms.home", "src/test/resources");
        assertTrue(new File(System.getProperty("opennms.home") + File.separator + "etc" + File.separator + "local-jasper-reports.xml").canRead());
    }
    
    @Test
    public void getValuesForSampleReportTest () {
        this.m_defaultLocalJasperReportsDao = new LegacyLocalJasperReportsDao();
        assertEquals("sample-report.jrxml", m_defaultLocalJasperReportsDao.getTemplateLocation("sample-report"));
        assertEquals("jdbc", m_defaultLocalJasperReportsDao.getEngine("sample-report"));
    }
    
    @Test
    public void getValuesForTrivialReportTest () {
        this.m_defaultLocalJasperReportsDao = new LegacyLocalJasperReportsDao();
        assertEquals("trivial-report.jrxml", m_defaultLocalJasperReportsDao.getTemplateLocation("trivial-report"));
        assertEquals("null", m_defaultLocalJasperReportsDao.getEngine("trivial-report"));
    }
    
    @Test
    public void getTemplateForSampleReportAsStreamTest () throws IOException {
        this.m_defaultLocalJasperReportsDao = new LegacyLocalJasperReportsDao();
        InputStream m_templateStream = m_defaultLocalJasperReportsDao.getTemplateStream("sample-report");
        assertEquals("check filesize by availiable call", 4822, m_templateStream.available());
    }
}
