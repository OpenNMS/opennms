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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.parameter.ReportParameters;

//TODO tak: In JasperReportService we use a DefaultGlobalRepository. Tests have to mockup the GlobalReportRepository
@Ignore
public class JasperReportServiceGetParametersTest {

    private JasperReportService service;

    @Before
    public void setUp() {
        System.setProperty("opennms.home", "features/reporting/jasper-reports/src/test/resources");
        service = new JasperReportService();
        // service.setReportRepository(new GlobalReportRepository());
    }

    @Test
    public void readPropertiesOfTrivialTestReportFromRESTRepoTest() throws ReportException {
        String id = "REMOTE_trivialJasperReport";
        assertNotNull(service.getParameters(id));
        ReportParameters params = service.getParameters(id);
        assertEquals(0, params.getReportParms().size());
    }

    @Test
    public void readPropertiesOfPropertyTestReportFromRESTRepoTest() throws ReportException {
        String id = "REMOTE_parameterTestJasperReport";
        assertNotNull(service.getParameters(id));
        ReportParameters params = service.getParameters(id);
        assertEquals(7, params.getReportParms().size());
    }

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
        assertEquals(7, params.getReportParms().size());
    }
 
    @Test
    public void readPropertiesOfJasperUriTest() throws ReportException {
        String id = "REMOTE_jasper-uri-test";
        assertNotNull(service.getParameters(id));
        ReportParameters params = service.getParameters(id);
        assertEquals(1, params.getReportParms().size());
    }
    
    @Test
    public void readPropertiesOfJasperResourceInputStreamURITest() throws ReportException {
        String id = "REMOTE_jasper-resource-inputstream-uri-test";
        assertNotNull(service.getParameters(id));
        ReportParameters params = service.getParameters(id);
        assertEquals(7, params.getReportParms().size());
    }
}
