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
package org.opennms.features.graphml.service.impl;

import static org.opennms.features.graphml.service.impl.GraphmlRepositoryImpl.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.graphdrawing.graphml.GraphmlType;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.core.xml.JaxbUtils;

public class GraphmlRepositoryImplTest {

    @Before
    public void setup() {
        System.setProperty("opennms.home", "target");
    }

    @Test
    public void testCreateReadDelete() throws Exception {
        final String NAME = "test-graph";

        // Create
        GraphmlRepositoryImpl graphmlRepository = new GraphmlRepositoryImpl();
        final InputStream graphmlStream = getClass().getResourceAsStream("/test-graph.xml");
        final GraphmlType graphmlType = JaxbUtils.unmarshal(GraphmlType.class, graphmlStream);
        graphmlRepository.save(NAME, "Label *yay*", graphmlType);

        // Verify that xml was generated
        Assert.assertEquals(true, graphmlRepository.exists(NAME));

        // Verify Topology cfg
        Properties properties = new Properties();
        properties.load(new FileInputStream(buildTopologyCfgFilepath(NAME)));
        Assert.assertEquals("Label *yay*", properties.get(LABEL));
        Assert.assertEquals(buildGraphmlFilepath(NAME), properties.get(TOPOLOGY_LOCATION));

        // Verify Graph API 2.0 cfg
        properties.load(new FileInputStream(buildGraphCfgFilepath(NAME)));
        Assert.assertEquals("Label *yay*", properties.get(LABEL));
        Assert.assertEquals(buildGraphmlFilepath(NAME), properties.get(GRAPH_LOCATION));

        // Read
        GraphmlType byName = graphmlRepository.findByName(NAME);

        // Verify Read
        final String graphmlTypeString = JaxbUtils.marshal(graphmlType);
        final String byNameString = JaxbUtils.marshal(byName);

        // The GraphML java classes are generated and do not
        // overwrite equals() and hashCode() methods.
        // We have to check for equality like this
        XmlTest.initXmlUnit();
        XmlTest.assertXmlEquals(graphmlTypeString,  byNameString);

        // Delete
        graphmlRepository.delete(NAME);
        Assert.assertEquals(false, graphmlRepository.exists(NAME));
        Assert.assertThat(Files.exists(Paths.get(buildGraphmlFilepath(NAME))), Matchers.is(false));
        Assert.assertThat(Files.exists(Paths.get(buildTopologyCfgFilepath(NAME))), Matchers.is(false));
        Assert.assertThat(Files.exists(Paths.get(buildGraphCfgFilepath(NAME))), Matchers.is(false));
    }
}
