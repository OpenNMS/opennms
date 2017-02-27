/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.graphml.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Properties;

import javax.xml.bind.JAXB;

import org.graphdrawing.graphml.GraphmlType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;

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
        GraphmlType graphmlType = JAXB.unmarshal(getClass().getResource("/v1/test-graph.xml"), GraphmlType.class);
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
        ByteArrayOutputStream graphmlTypeOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream byNameOutputStream = new ByteArrayOutputStream();
        JAXB.marshal(graphmlType, graphmlTypeOutputStream);
        JAXB.marshal(byName, byNameOutputStream);

        // The GraphML java classes are generated and do not
        // overwrite equals() and hashCode() methods.
        // We have to check for equality like this
        XmlTest.initXmlUnit();
        XmlTest.assertXmlEquals(
                new String(graphmlTypeOutputStream.toByteArray()),
                new String(byNameOutputStream.toByteArray())
        );

        // Delete
        graphmlRepository.delete(NAME);
        Assert.assertEquals(false, graphmlRepository.exists(NAME));
        Assert.assertEquals(false, graphmlRepository.exists(NAME));
    }
}
