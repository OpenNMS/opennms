/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.measurements.model.Expression;
import org.opennms.netmgt.measurements.model.FilterDef;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests the Measurements API with an JRB backend.
 *
 * @author Jesse White <jesse@opennms.org>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-measurement.xml",
        "classpath:/META-INF/opennms/applicationContext-measurements-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.rrd.strategyClass=org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy"
})
@JUnitTemporaryDatabase(reuseDatabase=false) // relies on setUp()
@Transactional
public class MeasurementsRestServiceWithJrbIT extends MeasurementsRestServiceITCase {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementsRestServiceWithJrbIT.class);

    @Before
    public void setUp() {
        super.setUp();

        File rrdDirectory = new File("src/test/resources/share/jrb");
        assertTrue(rrdDirectory.canRead());

        m_resourceStorageDao.setRrdDirectory(rrdDirectory);
        System.setProperty("rrd.base.dir", rrdDirectory.getAbsolutePath());
    }

    @Test
    public void canRetrieveMeasurementsFromJrb() {
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

    @Test
    public void canPerformExpressions() {
        QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);
        request.setMaxRows(700);

        Source ifInOctets = new Source();
        ifInOctets.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        ifInOctets.setAttribute("ifInOctets");
        ifInOctets.setAggregation("MAX");
        ifInOctets.setLabel("ifInOctets");
        request.setSources(Lists.newArrayList(ifInOctets));

        Expression scale = new Expression();
        scale.setLabel("ifUsage");
        // References a variable from strings.properties
        scale.setExpression("ifInOctets * 8 / ifInOctets.ifSpeed");
        request.setExpressions(Lists.newArrayList(scale));

        QueryResponse response = m_svc.query(request);

        final int idx = 3;
        final Map<String, double[]> columns = response.columnsWithLabels();
        assertEquals(975.3053156146178, columns.get("ifInOctets")[idx], 0.0001);
        assertEquals(975.3053156146178 * 8d / 1000.0d, columns.get("ifUsage")[idx], 0.0001);
    }

    @Test
    public void canApplyFilters() {
        QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1418046400000L);
        request.setStep(1000L);
        request.setMaxRows(700);

        Source ifInOctets = new Source();
        ifInOctets.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        ifInOctets.setAttribute("ifInOctets");
        ifInOctets.setAggregation("MAX");
        ifInOctets.setLabel("ifInOctets");
        request.setSources(Lists.newArrayList(ifInOctets));

        // Apply a chomp filter - cutting some of the first row off, and the trailing NaNs
        FilterDef chompFilter = new FilterDef("Chomp",
                "cutoffDate", "1414630000.0",
                "stripNaNs", "true");
        request.setFilters(Lists.newArrayList(chompFilter));
        LOG.debug(JaxbUtils.marshal(request));

        QueryResponse response = m_svc.query(request);

        // Verify the values for the first and last rows
        final Map<String, double[]> columns = response.columnsWithLabels();
        double ifInOctetsColumn[] = columns.get("ifInOctets");
        assertEquals(67872.22455490529, ifInOctetsColumn[0], 0.0001);
        assertEquals(1649961.9593111263, ifInOctetsColumn[ifInOctetsColumn.length-1], 0.0001);
    }
}
