/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.test.xml.JsonTest;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.NF5Packet;
import org.opennms.netmgt.flows.api.NF5Record;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.searchbox.client.AbstractJestClient;

public class FlowDocumentTest {

    private final Gson gson = new GsonBuilder()
            .setDateFormat(AbstractJestClient.ELASTIC_SEARCH_DATE_FORMAT)
            .create();

    private DocumentEnricher enricher;

    @Before
    public void setUp() {
        final MockDocumentEnricherFactory factory = new MockDocumentEnricherFactory();
        enricher = factory.getEnricher();

        final NodeDao nodeDao = factory.getNodeDao();
        final InterfaceToNodeCache interfaceToNodeCache = factory.getInterfaceToNodeCache();
        interfaceToNodeCache.setNodeId("SomeLocation", InetAddressUtils.addr("192.168.1.2"), 1);
        interfaceToNodeCache.setNodeId("SomeLocation", InetAddressUtils.addr("192.168.2.2"), 2);
        interfaceToNodeCache.setNodeId("SomeLocation", InetAddressUtils.addr("192.168.1.1"), 3);

        nodeDao.save(createOnmsNode(1, "SomeRequisition"));
        nodeDao.save(createOnmsNode(2, "SomeRequisition"));
        nodeDao.save(createOnmsNode(3, "SomeRequisition"));
    }

    @Test
    public void verifyEffectiveNetflow5Document() throws IOException {
        // Convert
        final Netflow5Converter converter = new Netflow5Converter();
        final List<FlowDocument> documents = converter.convert(getMockNetflow5Packet());

        // Enrich
        enricher.enrich(documents, getMockFlowSource());

        // Serialize
        assertThat(documents, hasSize(1));
        final FlowDocument document = documents.get(0);
        String actualJson = gson.toJson(document);

        // Verify
        final String expectedJson = Resources.toString(Resources.getResource("flow-document-netflow5.json"), StandardCharsets.UTF_8);
        JsonTest.assertJsonEquals(expectedJson, actualJson);
    }

    public static NF5Packet getMockNetflow5Packet() {
        final NF5Packet packet = mock(NF5Packet.class);
        final NF5Record record = mock(NF5Record.class);
        when(record.getSrcAddr()).thenReturn("192.168.1.2");
        when(record.getDstAddr()).thenReturn("192.168.2.2");
        final List<? extends NF5Record> records = Lists.newArrayList(record);
        Mockito.doReturn(records).when(packet).getRecords();
        return packet;
    }

    public static FlowSource getMockFlowSource() {
        final FlowSource flowSource = mock(FlowSource.class);
        when(flowSource.getLocation()).thenReturn("SomeLocation");
        when(flowSource.getSourceAddress()).thenReturn("192.168.1.1");
        return flowSource;
    }

    private static OnmsNode createOnmsNode(int nodeId, String foreignSource) {
        final OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        node.setForeignSource(foreignSource);
        node.setForeignId(nodeId + "");

        final OnmsCategory category = new OnmsCategory();
        category.setName("SomeCategory");
        node.setCategories(Sets.newHashSet(category));
        return node;
    }

}
