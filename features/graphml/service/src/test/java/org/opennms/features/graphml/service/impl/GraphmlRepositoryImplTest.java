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

package org.opennms.features.graphml.service.impl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.opennms.features.graphml.service.impl.GraphmlRepositoryImpl.GRAPH_CFG_FILE_PREFIX;
import static org.opennms.features.graphml.service.impl.GraphmlRepositoryImpl.GRAPH_LOCATION;
import static org.opennms.features.graphml.service.impl.GraphmlRepositoryImpl.LABEL;
import static org.opennms.features.graphml.service.impl.GraphmlRepositoryImpl.TOPOLOGY_LOCATION;
import static org.opennms.features.graphml.service.impl.GraphmlRepositoryImpl.buildGraphmlFilepath;
import static org.opennms.features.graphml.service.impl.GraphmlRepositoryImpl.buildTopologyCfgFilepath;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.graphdrawing.graphml.GraphmlType;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;

public class GraphmlRepositoryImplTest {

    final static String NAME = "test-graph";

    @Before
    public void setup() throws IOException {
        System.setProperty("opennms.home", "target");
        // make sure we start with a clean state:
        Files.deleteIfExists(Paths.get(buildGraphmlFilepath(NAME)));
        Files.deleteIfExists(Paths.get(buildTopologyCfgFilepath(NAME)));
    }

    @Test
    public void testCreateReadDelete() throws Exception {

        // Create
        ConfigurationManagerService cm = Mockito.mock(ConfigurationManagerService.class);
        ArgumentCaptor<JsonAsString> jsonCaptor = ArgumentCaptor.forClass(JsonAsString.class);
        GraphmlRepositoryImpl graphmlRepository = new GraphmlRepositoryImpl(cm);
        final InputStream graphmlStream = getClass().getResourceAsStream("/test-graph.xml");
        final GraphmlType graphmlType = JaxbUtils.unmarshal(GraphmlType.class, graphmlStream);
        graphmlRepository.save(NAME, "Label *yay*", graphmlType);
        verify(cm).registerConfiguration(eq(GRAPH_CFG_FILE_PREFIX), eq(NAME), jsonCaptor.capture());

        // Verify that xml was generated
        Assert.assertTrue(graphmlRepository.exists(NAME));

        // Verify Topology cfg
        Properties properties = new Properties();
        properties.load(new FileInputStream(buildTopologyCfgFilepath(NAME)));
        Assert.assertEquals("Label *yay*", properties.get(LABEL));
        Assert.assertEquals(buildGraphmlFilepath(NAME), properties.get(TOPOLOGY_LOCATION));

        // Verify Graph API 2.0 cfg
        JSONObject json = new JSONObject(jsonCaptor.getValue().toString());
        Assert.assertEquals(buildGraphmlFilepath(NAME), json.get(GRAPH_LOCATION));

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
        verify(cm).unregisterConfiguration(GRAPH_CFG_FILE_PREFIX, NAME);
    }
}
