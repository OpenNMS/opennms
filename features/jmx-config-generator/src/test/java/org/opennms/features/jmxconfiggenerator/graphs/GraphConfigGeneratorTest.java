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
import org.junit.Test;
import org.opennms.features.jmxconfiggenerator.log.Slf4jLogAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class GraphConfigGeneratorTest {

    // Verify that the default snmp-graph.properties template is not deleted
    @Test
    public void verifySnmpGraphTemplate() throws IOException {
        final String templatePath = GraphConfigGenerator.INTERN_TEMPLATE_NAME;
        InputStream templateInputStream = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if (templateInputStream == null) {
            throw new IOException(String.format("Template file '%s' doesn't exist.", GraphConfigGenerator.INTERN_TEMPLATE_NAME));
        }
    }

    @Test
    public void verifyGraphGeneration() {
        JmxConfigReader jmxConfigReader = new JmxConfigReader(new Slf4jLogAdapter(JmxConfigReader.class));
        Collection<Report> reports = jmxConfigReader.generateReportsByJmxDatacollectionConfig(getClass().getResourceAsStream("/cassandra21x-datacollection-config.xml"));
        GraphConfigGenerator graphConfigGenerator = new GraphConfigGenerator(new Slf4jLogAdapter(GraphConfigGenerator.class));
        String snmpGraph = graphConfigGenerator.generateSnmpGraph(reports);
        Assert.assertNotNull(snmpGraph);
    }
}
