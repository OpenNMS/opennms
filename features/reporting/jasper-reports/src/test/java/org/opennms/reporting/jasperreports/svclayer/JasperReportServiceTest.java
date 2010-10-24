/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 22nd 2010 jonathan@opennms.org
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.reporting.jasperreports.svclayer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.api.reporting.ReportException;
import org.opennms.api.reporting.ReportFormat;
import org.opennms.api.reporting.parameter.ReportParameters;
import org.opennms.netmgt.dao.JasperReportConfigDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners( { DependencyInjectionTestExecutionListener.class })
@ContextConfiguration(locations = { "classpath:org/opennms/reporting/jasperreports/svclayer/JasperReportServiceTest.xml" })
public class JasperReportServiceTest {

	@Autowired
	JasperReportConfigDao m_configDao;
	@Autowired
	JasperReportService m_reportService;

	private static final String REPORTID = "parameter-test";
	private static final String BOGUSREPORTID = "bogus-parameter-test";

	@BeforeClass
	public static void setupOnmsHome() {
		System.setProperty("opennms.home", "src/test/resources");

	}

	@Test
	public void testWiring() {
		Assert.assertNotNull(m_configDao);
		Assert.assertNotNull(m_reportService);
	}

	@Test
	public void testGetParmeters() {
		ReportParameters reportParameters = m_reportService
				.getParameters(REPORTID);
		Assert.assertNotNull(reportParameters);
		Assert.assertEquals(1, reportParameters.getIntParms().size());
		Assert.assertEquals(1, reportParameters.getStringParms().size());
		Assert.assertEquals(2, reportParameters.getDateParms().size());
	}

	// TODO write test for bogus parms in here.

	@Test
	public void testValidate() {
		HashMap<String, Object> reportParms = new HashMap<String, Object>();
		Assert.assertTrue(m_reportService.validate(reportParms, REPORTID));
	}

	@Test
	public void testRunAndRender() {
		HashMap<String, Object> reportParms;
		reportParms = new HashMap<String, Object>();
		reportParms.put("stringParameter", new String("report"));
		reportParms.put("integerParameter", new Integer(1));
		reportParms.put("dateParameter", new java.util.Date());
		java.util.Date date = new java.util.Date();
		reportParms.put("dateParamter", date);
		reportParms.put("sqlDateParameter", new java.util.Date(date.getTime()));
		try {
			m_reportService.runAndRender(reportParms, REPORTID,
					ReportFormat.PDF, new NullOutputStream());
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
