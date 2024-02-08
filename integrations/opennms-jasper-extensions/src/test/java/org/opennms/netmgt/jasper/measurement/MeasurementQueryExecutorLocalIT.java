/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.jasper.measurement;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;

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

import net.sf.jasperreports.engine.JRException;

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
