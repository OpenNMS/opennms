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
