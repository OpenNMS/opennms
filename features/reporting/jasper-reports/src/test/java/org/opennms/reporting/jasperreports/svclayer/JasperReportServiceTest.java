/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.reporting.jasperreports.svclayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.parameter.ReportDoubleParm;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.api.JasperReportConfigDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@Ignore
//TODO tak: We have replaced the jasperReportConfigDao is replaced by a GlobalReportRepository. Test has to mockup the GlobalReportRepository
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath:org/opennms/reporting/jasperreports/svclayer/JasperReportServiceTest.xml" })
public class JasperReportServiceTest implements InitializingBean {

    @Autowired
    JasperReportConfigDao m_configDao;
    @Autowired
    JasperReportService m_reportService;

    private static final String REPORTID = "parameter-test";

    @BeforeClass
    public static void setupOnmsHome() {
        System.setProperty("opennms.home", "src/test/resources");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    public void testGetParmeters() {
        ReportParameters reportParameters;
        try {
            reportParameters = m_reportService.getParameters(REPORTID);
            assertNotNull(reportParameters);
            assertEquals(1, reportParameters.getIntParms().size());
            assertEquals(1, reportParameters.getFloatParms().size());
            assertEquals(1, reportParameters.getDoubleParms().size());
            assertEquals(2, reportParameters.getStringParms().size());
            assertEquals(3, reportParameters.getDateParms().size());
        } catch (ReportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testValidate() {
        HashMap<String, Object> reportParms = new HashMap<String, Object>();
        Assert.assertTrue(m_reportService.validate(reportParms, REPORTID));
    }

    @Test
    public void testDescriptions() {
        ReportParameters reportParameters;
        try {
            reportParameters = m_reportService.getParameters(REPORTID);
            ReportStringParm stringParm1 = reportParameters.getStringParms().get(0);
            Assert.assertEquals("a string parameter", stringParm1.getDisplayName());
            ReportStringParm stringParm2 = reportParameters.getStringParms().get(1);
            Assert.assertEquals("stringParameter2", stringParm2.getDisplayName());
            Assert.assertEquals(3, reportParameters.getDateParms().size());
        } catch (ReportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testStringInputType() {
        ReportParameters reportParameters;
        try {
            reportParameters = m_reportService.getParameters(REPORTID);
            ReportStringParm stringParm2 = reportParameters.getStringParms().get(1);
            Assert.assertEquals("onmsCategorySelector", stringParm2.getInputType());
        } catch (ReportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Using a test date of -112426200000L ( Thu, 09 Jun 1966 18:30:00)
     */
    @Test
    public void testDefaults() {
        ReportParameters reportParameters;
        try {
            reportParameters = m_reportService.getParameters(REPORTID);
            ReportStringParm stringParm1 = reportParameters.getStringParms().get(0);
            Assert.assertEquals("Hosts", stringParm1.getValue());
            ReportStringParm stringParm2 = reportParameters.getStringParms().get(1);
            Assert.assertEquals("Routers", stringParm2.getValue());
            ReportFloatParm floatParm = reportParameters.getFloatParms().get(0);
            Assert.assertEquals(new Float("99.99"), floatParm.getValue());
            ReportIntParm intParm = reportParameters.getIntParms().get(0);
            Assert.assertEquals(100, intParm.getValue());
            ReportDoubleParm doubleParm = reportParameters.getDoubleParms().get(0);
            Assert.assertEquals(new Double("99.99"), doubleParm.getValue());

            // TODO Tak: why is this off?
            // ReportDateParm dateParm1 = reportParameters.getDateParms().get(0);
            // Assert.assertEquals(19, dateParm1.getHours().intValue());
            // Assert.assertEquals(30, dateParm1.getMinutes().intValue());

        } catch (ReportException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testRunAndRender() {
        HashMap<String, Object> reportParms;
        reportParms = new HashMap<String, Object>();
        reportParms.put("stringParameter1", new String("string1"));
        reportParms.put("stringParameter2", new String("string2"));
        reportParms.put("integerParameter", new Integer(1));
        reportParms.put("floatParameter", new Float("0.5"));
        reportParms.put("doubleParameter", new Double("0.5"));
        reportParms.put("dateParameter", new java.util.Date());
        java.util.Date date = new java.util.Date();
        reportParms.put("dateParamter", date);
        reportParms.put("sqlDateParameter", new java.util.Date(date.getTime()));
        reportParms.put("sqlTimestampParameter", new java.util.Date(date.getTime()));
        try {
            m_reportService.runAndRender(reportParms, REPORTID, ReportFormat.PDF, new NullOutputStream());
        } catch (ReportException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testRunAndRenderCSV() {
        HashMap<String, Object> reportParms;
        reportParms = new HashMap<String, Object>();
        reportParms.put("stringParameter1", new String("string1"));
        reportParms.put("stringParameter2", new String("string2"));
        reportParms.put("integerParameter", new Integer(1));
        reportParms.put("floatParameter", new Float("0.5"));
        reportParms.put("doubleParameter", new Double("0.5"));
        reportParms.put("dateParameter", new java.util.Date());
        java.util.Date date = new java.util.Date();
        reportParms.put("dateParamter", date);
        reportParms.put("sqlDateParameter", new java.util.Date(date.getTime()));
        reportParms.put("sqlTimestampParameter", new java.util.Date(date.getTime()));
        try {
            m_reportService.runAndRender(reportParms, REPORTID, ReportFormat.CSV, new NullOutputStream());
        } catch (ReportException e) {
            Assert.fail(e.toString());
        }
    }

    /** Writes to nowhere */
    private class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
        }
    }
}
