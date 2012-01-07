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

package org.opennms.features.reporting.repository.local;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore
public class LegacyLocalReportRepositoryTest {
    Logger logger = LoggerFactory.getLogger(LegacyLocalReportRepositoryTest.class);
    ReportRepository m_repo = new LegacyLocalReportRepository();
    
    @BeforeClass
    public static void setup() {
        System.setProperty("opennms.home", "src/test/resources");
    }
    
    @Test
    public void reportIdsWithRepositoryIdsTest() {
        assertEquals("local", m_repo.getRepositoryId());
        BasicReportDefinition report = m_repo.getReports().get(0);
        logger.debug(report.getId());
        logger.debug(m_repo.getDisplayName(report.getId()));
    }
    
    @Test
    public void getReportsTest() {
        List<BasicReportDefinition> reports = m_repo.getReports();
        for (BasicReportDefinition report : reports) {
	        logger.debug("'{}' \t '{}'", report.getId(), report.getReportService());
	    }
    }  
}