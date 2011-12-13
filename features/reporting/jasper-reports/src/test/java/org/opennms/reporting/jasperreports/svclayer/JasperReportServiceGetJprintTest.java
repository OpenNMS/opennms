/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2010-2011 The OpenNMS Group,
 * Inc. OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.reporting.jasperreports.svclayer;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.opennms.api.reporting.ReportException;

public class JasperReportServiceGetJprintTest {

    private JasperReportService service;
    
    @Before
    public void setUp() {
        System.setProperty("opennms.home", "/opt/opennms");
        System.setProperty("opennms.report.dir", "/tmp");
        service = new JasperReportService();
    }
    
    @Test
    public void runReportFromLocalRepoTest() throws ReportException {
        assertTrue(service.run(new HashMap<String, Object>(), "trivial-report").startsWith("/tmp/trivial-report"));
    }
    
    @Test
    public void runReportFromRemoteRepoTest() throws ReportException {
        assertTrue(service.run(new HashMap<String, Object>(), "REMOTE_trivialJasperReport").startsWith("/tmp/trivial-report"));
    }
}
