/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.graphdrawing.graphml.GraphmlType;
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

        // Verify cfg
        Properties properties = new Properties();
        properties.load(new FileInputStream(GraphmlRepositoryImpl.buildCfgFilepath(NAME)));
        Assert.assertEquals("Label *yay*", properties.get(GraphmlRepositoryImpl.LABEL));
        Assert.assertEquals(GraphmlRepositoryImpl.buildGraphmlFilepath(NAME), properties.get(GraphmlRepositoryImpl.TOPOLOGY_LOCATION));

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
        Assert.assertEquals(false, graphmlRepository.exists(NAME));
    }
}
