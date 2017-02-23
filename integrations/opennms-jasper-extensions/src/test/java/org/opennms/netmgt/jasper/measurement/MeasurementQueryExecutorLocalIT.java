/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.measurement;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

import net.sf.jasperreports.engine.JRException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.jasper.helper.MeasurementsHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * Verifies that the {@link MeasurementQueryExecutor} works correctly when running locally in JVM mode.
 * Verifies only selection of all available tests.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {"/META-INF/opennms/applicationContext-measurements-test.xml"}
)
public class MeasurementQueryExecutorLocalIT extends AbstractMeasurementQueryExecutorTest {

    @Inject
    private ApplicationContext context;


    @Before
    public void before() throws IOException {
        super.before();

        // init ApplicationContext
        BeanUtils.setStaticApplicationContext(context);

        // Ensure that we are running in jvm mode
        Assert.assertEquals(Boolean.TRUE, MeasurementsHelper.isRunInOpennmsJvm());
    }

    @After
    public void after() {
        super.after();

        // reset static ApplicationContext
        BeanUtils.setStaticApplicationContext(null);
    }

    @Test
    public void testReportAllCharts() throws IOException, JRException {
        createReport("AllChartsReport", new ReportFiller() {
            @Override
            public void fill(Map<String, Object> params) throws Exception {
                // We run in JVM mode, but set the url to verify that it is not called
                params.put("MEASUREMENT_URL", "http://localhost:9999/opennms/rest/measurements");
                params.put("startDate", String.valueOf(DATE_FORMAT.parse("Wed Aug 26 06:05:00 CEST 2015").getTime()));
                params.put("endDate",  String.valueOf(DATE_FORMAT.parse("Thu Aug 27 06:00:00 CEST 2015").getTime()));
            }
        });
        verifyHttpCalls(0);
    }

    @Test
    public void testReportRrdGraph() throws JRException, IOException {
        createReport("RrdGraph", new ReportFiller() {
            @Override
            public void fill(Map<String, Object> params) {
                // We run in JVM mode, but set the url to verify that it is not called
                params.put("MEASUREMENT_URL", "http://localhost:9999/opennms/rest/measurements");
                params.put("startDate", new Date("Wed Oct 13 17:25:00 EDT 2010").getTime());
                params.put("endDate", new Date("Wed Oct 13 21:16:30 EDT 2010").getTime());
            }
        });
        verifyHttpCalls(0);
    }
}
