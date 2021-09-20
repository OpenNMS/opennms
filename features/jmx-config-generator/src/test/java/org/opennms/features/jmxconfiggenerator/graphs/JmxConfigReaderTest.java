/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.graphs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.jmxconfiggenerator.log.Slf4jLogAdapter;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */

public class JmxConfigReaderTest {

    private JmxConfigReader jmxConfigReader;
    private GraphConfigGenerator graphConfigGenerator;

    @Before
    public void setUp() {
        jmxConfigReader = new JmxConfigReader(new Slf4jLogAdapter(JmxConfigReader.class));
        graphConfigGenerator = new GraphConfigGenerator(new Slf4jLogAdapter(GraphConfigGenerator.class));
    }

    @Test
    public void testGenerateReportsByJmxDatacollectionConfig() {
        Collection<Report> reports = jmxConfigReader.generateReportsByJmxDatacollectionConfig("src/test/resources/test.xml");
        Assert.assertEquals("read structure from test.xml", 7, reports.size());

        reports = jmxConfigReader.generateReportsByJmxDatacollectionConfig("src/test/resources/JVM-Basics.xml");
        Assert.assertEquals("read structure from JVM-Basics.xml", 117, reports.size());

        reports = jmxConfigReader.generateReportsByJmxDatacollectionConfig("src/test/resources/jmx-datacollection-config.xml");
        Assert.assertEquals("read structure from jmx-datacollection-config.xml", 139, reports.size());
    }

    @Test
    public void testVelociteyRun() throws IOException {
        Collection<Report> reports = jmxConfigReader.generateReportsByJmxDatacollectionConfig("src/test/resources/JVM-Basics.xml");
        String snmpGraphConfig = graphConfigGenerator.generateSnmpGraph(reports, "src/main/resources/graphTemplate.vm");
        Assert.assertNotNull(snmpGraphConfig);
    }
}
