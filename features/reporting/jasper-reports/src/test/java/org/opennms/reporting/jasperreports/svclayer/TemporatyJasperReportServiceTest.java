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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.features.reporting.repository.global.GlobalReportRepository;

public class TemporatyJasperReportServiceTest {

    private JasperReportService service = new JasperReportService();

    @Before
    public void setUp() {
        System.setProperty("opennms.home", "/opt/opennms");
        service.setReportRepository(new GlobalReportRepository());
    }
    
    @Ignore
    @Test
    public void readPropertiesOfTrivialTestReportFromRESTRepoTest() throws ReportException {
        String id = "REMOTE_trivialJasperReport";
        assertNotNull(service.getParameters(id));
        ReportParameters params = service.getParameters(id);
        assertEquals(0, params.getReportParms().size());
    }

    @Ignore
    @Test
    public void readPropertiesOfPropertyTestReportFromRESTRepoTest() throws ReportException {
        String id = "REMOTE_parameterTestJasperReport";
        assertNotNull(service.getParameters(id));
        ReportParameters params = service.getParameters(id);
        assertEquals(12, params.getReportParms().size());
    }

    @Ignore
    @Test
    public void readPropertiesOfTrivialTestReportTest() throws ReportException {
        String id = "trivial-report";
        assertNotNull(service.getParameters(id));
        ReportParameters params = service.getParameters(id);
        assertEquals(0, params.getReportParms().size());
    }

    @Test
    public void readPropertiesOfPropertyTestReportTest() throws ReportException {
        String id = "parameter-test";
        assertNotNull(service.getParameters(id));
        ReportParameters params = service.getParameters(id);
        assertEquals(12, params.getReportParms().size());
    }
}
