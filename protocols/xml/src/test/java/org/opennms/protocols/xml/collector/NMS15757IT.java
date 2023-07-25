/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

package org.opennms.protocols.xml.collector;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.MapScope;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.utils.RpcTargetHelper;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectorRequestBuilder;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.client.rpc.CollectorRequestBuilderImpl;
import org.opennms.netmgt.collection.client.rpc.CollectorRequestDTO;
import org.opennms.netmgt.collection.client.rpc.CollectorResponseDTO;
import org.opennms.netmgt.collection.client.rpc.LocationAwareCollectorClientImpl;
import org.opennms.netmgt.collection.dto.CollectionAttributeDTO;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.protocols.xml.config.XmlRrd;
import org.opennms.protocols.xml.config.XmlSource;
import org.opennms.protocols.xml.dao.XmlDataCollectionConfigDao;

public class NMS15757IT {

    protected NodeDao nodeDao;
    protected EntityScopeProvider entityScopeProvider;
    protected CollectionAgent collectionAgent;

    @Before
    public void before() throws Exception {
        nodeDao = mock(NodeDao.class);

        final OnmsNode onmsNode = new OnmsNode();
        onmsNode.setLabel("label");
        onmsNode.setForeignId("foreignId");
        onmsNode.setForeignSource("foreignSource");
        when(nodeDao.get(1)).thenReturn(onmsNode);

        collectionAgent = mock(CollectionAgent.class);
        when(collectionAgent.getNodeId()).thenReturn(1);
        when(collectionAgent.getAddress()).thenReturn(InetAddress.getByName("10.10.10.10"));
        when(collectionAgent.getLocationName()).thenReturn("Default");

        final Map<ContextKey, String> map = new HashMap<>();
        map.put(new ContextKey("scv", "xml:username"), "heinz");
        map.put(new ContextKey("scv", "xml:password"), "erhardt");
        final MapScope mapScope = new MapScope(Scope.ScopeName.DEFAULT, map);
        entityScopeProvider = mock(EntityScopeProvider.class);
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(mapScope);
        when(entityScopeProvider.getScopeForInterface(1, "10.10.10.10")).thenReturn(EmptyScope.EMPTY);
    }

    @Test
    public void testMetadata() throws Exception {
        final LocationAwareCollectorClientImpl locationAwareCollectorClient = new LocationAwareCollectorClientImpl() {
            @Override
            protected RpcClient<CollectorRequestDTO, CollectorResponseDTO> getDelegate() {
                return request -> {
                    final Map<String, Object> map = request.getAttributes().stream()
                            .filter(c -> c.getValueOrContents() != null)
                            .collect(Collectors.toMap(CollectionAttributeDTO::getKey, CollectionAttributeDTO::getValueOrContents));
                    checkAttributes(map);
                    return CompletableFuture.completedFuture(new CollectorResponseDTO());
                };
            }

            @Override
            public RpcTargetHelper getRpcTargetHelper() {
                return new RpcTargetHelper();
            }

            @Override
            public EntityScopeProvider getEntityScopeProvider() {
                return entityScopeProvider;
            }
        };

        final CollectorRequestBuilder collectorRequestBuilder = new CollectorRequestBuilderImpl(locationAwareCollectorClient)
                .withCollector(getCollector())
                .withAgent(collectionAgent)
                .withAttributes(getAttributes());

        collectorRequestBuilder.execute();
    }

    private Map<String, Object> getAttributes() {
        final Map<String, Object> map = new TreeMap<>();
        map.put("xml-collection", "my-collection");
        return map;
    }

    private ServiceCollector getCollector() {
        final XmlRrd xmlRrd = new XmlRrd();
        xmlRrd.setStep(300);

        final XmlSource xmlSource = new XmlSource();
        xmlSource.setUrl("https://${scv:xml:username}:${scv:xml:password}@foobar.org?bla=${xxx:yyy|default}");

        final XmlDataCollection xmlDataCollection = new XmlDataCollection();
        xmlDataCollection.setName("my-collection");
        xmlDataCollection.setXmlRrd(xmlRrd);
        xmlDataCollection.addXmlSource(xmlSource);

        final XmlDataCollectionConfig xmlDataCollectionConfig = new XmlDataCollectionConfig();
        xmlDataCollectionConfig.addDataCollection(xmlDataCollection);

        final XmlDataCollectionConfigDao xmlDataCollectionConfigDao = mock(XmlDataCollectionConfigDao.class);
        when(xmlDataCollectionConfigDao.getDataCollectionByName("my-collection")).thenReturn(xmlDataCollection);
        when(xmlDataCollectionConfigDao.getConfig()).thenReturn(xmlDataCollectionConfig);

        final XmlCollector xmlCollector = new XmlCollector();
        xmlCollector.setNodeDao(nodeDao);
        xmlCollector.setXmlCollectionDao(xmlDataCollectionConfigDao);

        return xmlCollector;
    }

    private void checkAttributes(final Map<String, Object> attributes) {
        XmlDataCollection xmlDataCollection = (XmlDataCollection) attributes.get("xmlDatacollection");
        assertEquals("https://heinz:erhardt@foobar.org?bla=default", xmlDataCollection.getXmlSources().get(0).getUrl());
    }
}
