/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.parameter.ReportDoubleParm;
import org.opennms.api.reporting.parameter.ReportFloatParm;
import org.opennms.api.reporting.parameter.ReportIntParm;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.api.reporting.parameter.ReportStringParm;
import org.opennms.core.spring.BeanUtils;
import org.opennms.reporting.jasperreports.filter.ParameterFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import com.google.common.io.ByteStreams;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignExpression;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath:/org/opennms/reporting/jasperreports/svclayer/JasperReportServiceTest.xml" })
public class JasperReportServiceTest implements InitializingBean {

    @Autowired
    JasperReportService m_reportService;

    private static final String REPORTID = "parameter-test";

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    public void testGetParmeters() throws ReportException {
        ReportParameters reportParameters = m_reportService.getParameters(REPORTID);
        assertNotNull(reportParameters);
        assertEquals(1, reportParameters.getIntParms().size());
        assertEquals(1, reportParameters.getFloatParms().size());
        assertEquals(1, reportParameters.getDoubleParms().size());
        assertEquals(2, reportParameters.getStringParms().size());
        assertEquals(3, reportParameters.getDateParms().size());
    }

    @Test
    public void testValidate() {
        HashMap<String, Object> reportParms = new HashMap<String, Object>();
        Assert.assertTrue(m_reportService.validate(reportParms, REPORTID));
    }

    @Test
    public void testDescriptions() throws ReportException {
        ReportParameters reportParameters = m_reportService.getParameters(REPORTID);
        ReportStringParm stringParm1 = reportParameters.getStringParms().get(0);
        Assert.assertEquals("a string parameter", stringParm1.getDisplayName());
        ReportStringParm stringParm2 = reportParameters.getStringParms().get(1);
        Assert.assertEquals("stringParameter2", stringParm2.getDisplayName());
        Assert.assertEquals(3, reportParameters.getDateParms().size());
    }

    @Test
    public void testStringInputType() throws ReportException {
        ReportParameters reportParameters = m_reportService.getParameters(REPORTID);
        ReportStringParm stringParm2 = reportParameters.getStringParms().get(1);
        Assert.assertEquals("onmsCategorySelector", stringParm2.getInputType());
    }

    /**
     * Using a test date of -112426200000L ( Thu, 09 Jun 1966 18:30:00)
     */
    @Test
    public void testDefaults() throws ReportException {
        ReportParameters reportParameters = m_reportService.getParameters(REPORTID);
        ReportStringParm stringParm1 = reportParameters.getStringParms().get(0);
        Assert.assertEquals("Hosts", stringParm1.getValue());
        ReportStringParm stringParm2 = reportParameters.getStringParms().get(1);
        Assert.assertEquals("Routers", stringParm2.getValue());
        ReportFloatParm floatParm = reportParameters.getFloatParms().get(0);
        Assert.assertEquals(Float.valueOf("99.99"), floatParm.getValue());
        ReportIntParm intParm = reportParameters.getIntParms().get(0);
        Assert.assertEquals(100, intParm.getValue());
        ReportDoubleParm doubleParm = reportParameters.getDoubleParms().get(0);
        Assert.assertEquals(Double.valueOf("99.99"), doubleParm.getValue());
    }

    @Test
    public void testRunAndRender() throws ReportException {
        HashMap<String, Object> reportParms = new HashMap<>();
        reportParms.put("stringParameter1", "string1");
        reportParms.put("stringParameter2", "string2");
        reportParms.put("integerParameter", Integer.valueOf(1));
        reportParms.put("floatParameter", Float.valueOf("0.5"));
        reportParms.put("doubleParameter", Double.valueOf("0.5"));
        reportParms.put("dateParameter", new Date());
        java.util.Date date = new Date();
        reportParms.put("dateParamter", date);
        reportParms.put("sqlDateParameter", new Date(date.getTime()));
        reportParms.put("sqlTimestampParameter", new Date(date.getTime()));
        m_reportService.runAndRender(reportParms, REPORTID, ReportFormat.PDF, ByteStreams.nullOutputStream());
    }

    @Test
    public void testRunAndRenderCSV() throws ReportException {
        HashMap<String, Object> reportParms = new HashMap<>();
        reportParms.put("stringParameter1", "string1");
        reportParms.put("stringParameter2", "string2");
        reportParms.put("integerParameter", Integer.valueOf(1));
        reportParms.put("floatParameter", Float.valueOf("0.5"));
        reportParms.put("doubleParameter", Double.valueOf("0.5"));
        reportParms.put("dateParameter", new Date());
        java.util.Date date = new Date();
        reportParms.put("dateParamter", date);
        reportParms.put("sqlDateParameter", new Date(date.getTime()));
        reportParms.put("sqlTimestampParameter", new Date(date.getTime()));
        m_reportService.runAndRender(reportParms, REPORTID, ReportFormat.CSV, ByteStreams.nullOutputStream());
    }

    @Test
    public void testFilters() {
        List<ParameterFilter> parameterFilterList = m_reportService.getParameterFilters();
        boolean success = false;
        for (ParameterFilter eachFilter : parameterFilterList) {
            if (eachFilter.getClass() == DefaultParameterFilter.class) {
                success = true;
            }
        }
        Assert.assertEquals("Expected a default parameter filter", Boolean.TRUE, success);
    }

    @Test
    public void testEvaluateToString() throws JRException {
        final JasperReport report = JasperCompileManager.compileReport(getClass().getResourceAsStream("/etc/report-templates/subreport-expression-visitor-test.jrxml"));

        Assert.assertEquals("/home/ulf/opennms-reports", JasperReportService.evaluateToString(report, new JRDesignExpression("$P{ONMS_REPORT_DIR}")));
        Assert.assertEquals("/home/ulf/opennms-reports/subreports/", JasperReportService.evaluateToString(report, new JRDesignExpression("$P{SUBREPORT_DIR}")));
        Assert.assertEquals("This is a test with number 10", JasperReportService.evaluateToString(report, new JRDesignExpression("$P{COMPLEX}")));
    }
}
