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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * Tests the Measurements API with an RRD backend.
 *
 * Requires rrdtool and binary compatibility with the test .rrds.
 *
 * @author Jesse White <jesse@opennms.org>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-measurement.xml",
        "classpath:/META-INF/opennms/applicationContext-measurements-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy"
})
@JUnitTemporaryDatabase
@Transactional
@Ignore
public class MeasurementsRestServiceWithRrdIT extends MeasurementsRestServiceITCase {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementsRestServiceWithRrdIT.class);
    private static final File s_rrdDirectory = new File("src/test/resources/share/rrd");

    @BeforeClass
    public static void preSetUp() {
        assertTrue(s_rrdDirectory.canRead());
        LOG.debug("using RRD base directory: {}", s_rrdDirectory.getAbsolutePath());
        System.setProperty("rrd.base.dir", s_rrdDirectory.getAbsolutePath());
        System.setProperty("rrd.binary", findRrdtool());

        assumeRrdtoolExists("jrrd");
    }

    @Before
    public void setUp() {
        m_resourceStorageDao.setRrdDirectory(s_rrdDirectory);
        ConfigurationTestUtils.setRelativeRrdBaseDirectory(s_rrdDirectory.toString());
        ConfigurationTestUtils.setRrdBinary(findRrdtool());
        super.setUp();
    }

    @Test
    public void canRetrieveMeasurementsFromRrd() throws Exception {
        QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);
        request.setMaxRows(700);

        // Average
        Source ifInOctetsAvg = new Source();
        ifInOctetsAvg.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        ifInOctetsAvg.setAttribute("ifInOctets");
        ifInOctetsAvg.setAggregation("AVERAGE");
        ifInOctetsAvg.setLabel("ifInOctetsAvg");

        // Min
        Source ifInOctetsMin = new Source();
        ifInOctetsMin.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        ifInOctetsMin.setAttribute("ifInOctets");
        ifInOctetsMin.setAggregation("MIN");
        ifInOctetsMin.setLabel("ifInOctetsMin");

        // Max
        Source ifInOctetsMax = new Source();
        ifInOctetsMax.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        ifInOctetsMax.setAttribute("ifInOctets");
        ifInOctetsMax.setAggregation("MAX");
        ifInOctetsMax.setLabel("ifInOctetsMax");

        request.setSources(Lists.newArrayList(
                ifInOctetsAvg,
                ifInOctetsMin,
                ifInOctetsMax
                ));

        // Perform the query
        QueryResponse response = m_svc.query(request);

        // Validate the results
        long timestamps[] = response.getTimestamps();
        final Map<String, double[]> columns = response.columnsWithLabels();

        assertEquals(3600000L, response.getStep());
        assertEquals(680, timestamps.length);

        // Verify the values at an arbitrary index
        final int idx = 8;
        assertEquals(1414630800000L, timestamps[idx]);
        assertEquals(270.66140826873385, columns.get("ifInOctetsAvg")[idx], 0.0001);
        assertEquals(259.54086378737543, columns.get("ifInOctetsMin")[idx], 0.0001);
        assertEquals(67872.22455490529, columns.get("ifInOctetsMax")[idx], 0.0001);
    }
}
