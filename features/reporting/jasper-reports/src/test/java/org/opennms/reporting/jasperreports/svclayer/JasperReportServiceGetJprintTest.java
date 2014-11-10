/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.reporting.jasperreports.svclayer;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;

//TODO tak: Build tests to run in src/test/resources and not in local specific tmp directories
public class JasperReportServiceGetJprintTest {

    private JasperReportService service;
    
    @BeforeClass
    public static void setUp() {
        System.setProperty("opennms.home", "src/test/resources");
        System.setProperty("opennms.report.dir", "/tmp");
    }

    @Before
    public void initialize () {
        service = new JasperReportService();
    }

    @Ignore
    @Test
    public void runAndRenderTrivialReportTest() throws ReportException {
        try {
            service.runAndRender(new HashMap<String, Object>(), "local_trivial-report", ReportFormat.PDF, new FileOutputStream("/tmp/trivial-report.pdf"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Ignore
    @Test
    public void runAndRenderSubreportTest() throws ReportException {
        try {
            service.runAndRender(new HashMap<String, Object>(), "local_main-subreport-test", ReportFormat.PDF, new FileOutputStream("/tmp/main-subreport.pdf"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    @Ignore
    @Test
    public void runReportFromRemoteRepoTest() throws ReportException {
        assertTrue(service.run(new HashMap<String, Object>(), "REMOTE_trivialJasperReport").startsWith("/tmp/trivial-report"));
    }
}
