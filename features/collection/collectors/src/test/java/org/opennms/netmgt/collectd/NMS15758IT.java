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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
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
import org.opennms.netmgt.config.HttpCollectionConfigFactory;
import org.opennms.netmgt.config.httpdatacollection.HttpCollection;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.config.httpdatacollection.Rrd;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

public class NMS15758IT {
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
        map.put(new ContextKey("scv", "http:username"), "heinz");
        map.put(new ContextKey("scv", "http:password"), "erhardt");
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
        map.put("http-collection", "my-collection");
        return map;
    }

    private ServiceCollector getCollector() throws Exception {
        final Rrd rrd = new Rrd();
        rrd.setStep(300);

        final HttpCollection httpCollection = new HttpCollection();
        httpCollection.setName("my-collection");
        httpCollection.setRrd(rrd);

        final HttpDatacollectionConfig httpDatacollectionConfig = new HttpDatacollectionConfig();
        httpDatacollectionConfig.getHttpCollection().add(httpCollection);

        InputStream rdr = new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<http-datacollection-config  \n" +
                "    xmlns:http-dc=\"http://xmlns.opennms.org/xsd/config/http-datacollection\" \n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "    xsi:schemaLocation=\"http://xmlns.opennms.org/xsd/config/http-datacollection http://www.opennms.org/xsd/config/http-datacollection-config.xsd\" \n" +
                "    rrdRepository=\"${install.share.dir}/rrd/snmp/\" >\n" +
                "  <http-collection name=\"my-collection\">\n" +
                "    <rrd step=\"300\">\n" +
                "      <rra>RRA:AVERAGE:0.5:1:8928</rra>\n" +
                "      <rra>RRA:AVERAGE:0.5:12:8784</rra>\n" +
                "      <rra>RRA:MIN:0.5:12:8784</rra>\n" +
                "      <rra>RRA:MAX:0.5:12:8784</rra>\n" +
                "    </rrd>\n" +
                "    <uris>\n" +
                "      <uri name=\"test-document-count\">\n" +
                "        <url path=\"/test/resources/httpcolltest.html\"\n" +
                "             user-agent=\"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/412 (KHTML, like Gecko) Safari/412\" \n" +
                "             matches=\".*([0-9]+).*\" response-range=\"100-399\" >\n" +
                "           <parameters>" +
                "             <parameter key=\"password\" value=\"${scv:http:password}\"/>" +
                "           </parameters>" +
                "        </url>\n" +
                "        <attributes>\n" +
                "          <attrib alias=\"documentCount\" match-group=\"1\" type=\"counter32\"/>\n" +
                "        </attributes>\n" +
                "      </uri>\n" +
                "    </uris>\n" +
                "  </http-collection>\n" +
                "</http-datacollection-config>").getBytes(StandardCharsets.UTF_8));

        final HttpCollectionConfigFactory c = new HttpCollectionConfigFactory(rdr) {
            {{
                initialized = true;
                setInstance(this);
            }}
        };

        return new HttpCollector();
    }

    private void checkAttributes(final Map<String, Object> attributes) {
        HttpCollection httpCollection = (HttpCollection) attributes.get("httpCollection");
        assertEquals("Something went wrong", "erhardt", (String) httpCollection.getUris().get(0).getUrl().getParameters().get(0).getValue());
    }
}
