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

package org.opennms.web.rest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.measurements.model.Expression;
import org.opennms.web.rest.measurements.model.QueryRequest;
import org.opennms.web.rest.measurements.model.QueryResponse;
import org.opennms.web.rest.measurements.model.Source;
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
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-measurements-test-rrd.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
@Ignore
public class MeasurementsRestServiceWithRrdTest extends MeasurementsRestServiceTest {

    @Before
    public void setUp() {
        super.setUp();

        File rrdDirectory = new File("src/test/resources/share/rrd");
        assertTrue(rrdDirectory.canRead());

        m_resourceDao.setRrdDirectory(rrdDirectory);
        System.setProperty("rrd.base.dir", rrdDirectory.getAbsolutePath());
        System.setProperty("rrd.binary", "rrdtool");
    }

    @Test
    public void canRetrieveMeasurementsFromRrd(){
        QueryRequest request = new QueryRequest();
        request.setStart(1414602000000L);
        request.setEnd(1417046400000L);
        request.setStep(1000L);
        request.setMaxRows(350);

        Source ifInOctets = new Source();
        ifInOctets.setResourceId("node[1].interfaceSnmp[eth0-04013f75f101]");
        ifInOctets.setAttribute("ifInOctets");
        ifInOctets.setAggregation("AVERAGE");
        ifInOctets.setLabel("octetsIn");
        request.setSources(Lists.newArrayList(ifInOctets));

        Expression eightAsConstant = new Expression();
        eightAsConstant.setLabel("eight");
        eightAsConstant.setExpression("8");
        eightAsConstant.setTransient(true);

        Expression octetsToBytes = new Expression();
        octetsToBytes.setLabel("Bits In");
        octetsToBytes.setExpression("octetsIn * eight");
        request.setExpressions(Lists.newArrayList(eightAsConstant, octetsToBytes));

        QueryResponse response = m_svc.query(request);

        // Validate the results
        long timestamps[] = response.getTimestamps();
        final Map<String, double[]> columns = response.columnsWithLabels();

        assertEquals(7200000L, response.getStep());
        assertEquals(341, timestamps.length);

        // Verify the values at an arbitrary index
        final int idx = 1;
        assertEquals(1414612800000L, timestamps[idx]);
        assertEquals(4455.846126, columns.get("octetsIn")[idx], 0.0001);
        assertEquals(4455.846126 * 8, columns.get("Bits In")[idx], 0.0001);
        assertFalse("Transient values should be excluded.", columns.containsKey("eight"));
    }
}
