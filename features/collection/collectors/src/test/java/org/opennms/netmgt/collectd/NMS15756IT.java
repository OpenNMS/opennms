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
package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.opennms.netmgt.collection.client.rpc.CollectorRequestBuilderImpl;
import org.opennms.netmgt.collection.client.rpc.CollectorRequestDTO;
import org.opennms.netmgt.collection.client.rpc.CollectorResponseDTO;
import org.opennms.netmgt.collection.client.rpc.LocationAwareCollectorClientImpl;
import org.opennms.netmgt.collection.dto.CollectionAttributeDTO;
import org.opennms.netmgt.config.JMXDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.jmx.JmxConfig;
import org.opennms.netmgt.config.jmx.MBeanServer;
import org.opennms.netmgt.config.jmx.Parameter;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.model.OnmsNode;

public class NMS15756IT {

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
        when(collectionAgent.getHostAddress()).thenReturn("10.10.10.10");
        when(collectionAgent.getLocationName()).thenReturn("Default");
        collectionAgent.setAttribute("port", "1099");

        final Map<ContextKey, String> map = new HashMap<>();
        map.put(new ContextKey("scv", "jmx:username"), "heinz");
        map.put(new ContextKey("scv", "jmx:password"), "erhardt");
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
        map.put("collection", "my-collection");
        return map;
    }

    private JMXCollector getCollector() {
        final MBeanServer mBeanServer = new MBeanServer();
        mBeanServer.setIpAddress("10.10.10.10");
        mBeanServer.setPort(0);
        final List<Parameter> parameters = new ArrayList<>();
        final Parameter p1 = new Parameter();
        p1.setKey("username");
        p1.setValue("${scv:jmx:username}");
        parameters.add(p1);
        final Parameter p2 = new Parameter();
        p2.setKey("password");
        p2.setValue("${scv:jmx:password}");
        parameters.add(p2);
        mBeanServer.setParameters(parameters);
        final Set<MBeanServer> mBeanServers = new HashSet<>();
        mBeanServers.add(mBeanServer);
        final JmxConfig jmxConfig = new JmxConfig();
        jmxConfig.setMBeanServer(mBeanServers);

        final JmxConfigDao jmxConfigDao = new JmxConfigDao() {
            @Override
            public JmxConfig getConfig() {
                return jmxConfig;
            }
        };


        JMXDataCollectionConfigDao jmxDataCollectionConfigDao = new JMXDataCollectionConfigDao() {
            @Override
            public JmxCollection getJmxCollection(String collectionName) {
                final JmxCollection jmxCollection = new JmxCollection();
                jmxCollection.setName("my-collection");
                return jmxCollection;
            }
        };
        final JMXCollector jmxCollector = new JMXCollector();
        jmxCollector.setJmxDataCollectionConfigDao(jmxDataCollectionConfigDao);
        jmxCollector.setJmxConfigDao(jmxConfigDao);

        return jmxCollector;
    }

    private void checkAttributes(final Map<String, Object> attributes) {
        final MBeanServer xmlDataCollection = (MBeanServer) attributes.get("jmxMBeanServer");
        assertEquals("heinz", xmlDataCollection.getParameterMap().get("username"));
        assertEquals("erhardt", xmlDataCollection.getParameterMap().get("password"));
    }
}
