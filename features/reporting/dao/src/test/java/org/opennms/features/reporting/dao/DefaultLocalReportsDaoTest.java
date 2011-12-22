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

package org.opennms.features.reporting.dao;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultLocalReportsDaoTest {
    Logger logger = LoggerFactory.getLogger(DefaultLocalReportsDaoTest.class);
    
    @Before
    public void setup() {
        System.setProperty("opennms.home", "src/test/resources");
     }
    
    @Test
    public void setupTest() throws IOException {
        System.setProperty("opennms.home", "src/test/resources");
        assertEquals("src/test/resources", System.getProperty("opennms.home"));
        
        String pathToConfigXml = System.getProperty("opennms.home") + 
                File.separator + 
                "etc" + 
                File.separator + 
                "local-reports.xml";
        
        logger.debug("'{}'", pathToConfigXml);
        
        
        File testFile = new File(pathToConfigXml);
        assertTrue(testFile.exists());
        assertNotNull(testFile);
        assertTrue(testFile.canRead());
        assertTrue(testFile.canWrite());
        assertFalse(testFile.canExecute());
    }
    
    @Test
    public void getReportsCountTest () {
        LegacyLocalReportsDao m_dao = new LegacyLocalReportsDao();
        assertNotNull(m_dao);
        assertNotNull(m_dao.getReports());
        assertEquals(18, m_dao.getReports().size());
        
//        for (BasicReportDefinition report : m_dao.getReports()) {
//            logger.debug(report.getDisplayName());
//        }
    }
}
