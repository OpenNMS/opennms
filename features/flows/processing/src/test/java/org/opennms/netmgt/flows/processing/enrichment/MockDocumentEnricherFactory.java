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
package org.opennms.netmgt.flows.processing.enrichment;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.ScriptEngineManager;

import org.mockito.Mockito;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.dao.mock.MockAssetRecordDao;
import org.opennms.netmgt.dao.mock.MockCategoryDao;
import org.opennms.netmgt.dao.mock.MockInterfaceToNodeCache;
import org.opennms.netmgt.dao.mock.MockIpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.processing.impl.DocumentEnricherImpl;
import org.opennms.netmgt.flows.processing.impl.DocumentMangler;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfoCache;
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfoCacheImpl;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

public class MockDocumentEnricherFactory {

    private final NodeDao nodeDao;
    private final IpInterfaceDao ipInterfaceDao;
    private final InterfaceToNodeCache interfaceToNodeCache;
    private final MockAssetRecordDao assetRecordDao;
    private final MockCategoryDao categoryDao;
    private final DocumentEnricherImpl enricher;
    private final ClassificationEngine classificationEngine;

    private final AtomicInteger nodeDaoGetCounter = new AtomicInteger(0);

    public MockDocumentEnricherFactory() throws InterruptedException {
        this(0);
    }

    public MockDocumentEnricherFactory(final long clockSkewCorrectionThreshold) throws InterruptedException {
        nodeDao = createNodeDao();
        ipInterfaceDao = new MockIpInterfaceDao();
        interfaceToNodeCache = new MockInterfaceToNodeCache();
        assetRecordDao = new MockAssetRecordDao();
        categoryDao = new MockCategoryDao();

        classificationEngine = new DefaultClassificationEngine(() -> Lists.newArrayList(
                new RuleBuilder().withName("http").withDstPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withDstPort("443").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("http").withSrcPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withSrcPort("443").withProtocol("tcp,udp").build()
        ), FilterService.NOOP);
        final NodeInfoCache nodeInfoCache = new NodeInfoCacheImpl(
                new CacheConfigBuilder()
                        .withName("nodeInfoCache")
                        .withMaximumSize(1000)
                        .withExpireAfterWrite(300)
                        .withExpireAfterRead(300)
                        .build(),
                true,
                new MetricRegistry(),
                nodeDao,
                ipInterfaceDao,
                interfaceToNodeCache
        );
        enricher = new DocumentEnricherImpl(
                new MockSessionUtils(), classificationEngine,
                clockSkewCorrectionThreshold,
                new DocumentMangler(new ScriptEngineManager()), nodeInfoCache);

        // Required for mock node dao
        addServiceRegistry(nodeDao);
        addServiceRegistry(assetRecordDao);
        addServiceRegistry(categoryDao);
        DefaultServiceRegistry.INSTANCE.register(nodeDao, NodeDao.class);
        DefaultServiceRegistry.INSTANCE.register(assetRecordDao, AssetRecordDao.class);
        DefaultServiceRegistry.INSTANCE.register(categoryDao, CategoryDao.class);
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public InterfaceToNodeCache getInterfaceToNodeCache() {
        return interfaceToNodeCache;
    }

    public DocumentEnricherImpl getEnricher() {
        return enricher;
    }

    public AtomicInteger getNodeDaoGetCounter() {
        return nodeDaoGetCounter;
    }

    public ClassificationEngine getClassificationEngine() {
        return classificationEngine;
    }

    private NodeDao createNodeDao() {
        // Spy on MockNodeDao to count access to get(int)
        final MockNodeDao dao = new MockNodeDao();
        final NodeDao spyMock = Mockito.spy(dao);
        Mockito.when(spyMock.get(Mockito.anyInt())).then(invocationOnMock -> {
            nodeDaoGetCounter.incrementAndGet();
            return dao.get((int) invocationOnMock.getArguments()[0]);
        });
        return spyMock;
    }

    private void addServiceRegistry(OnmsDao dao) {
        try {
            Field field = AbstractMockDao.class.getDeclaredField("m_serviceRegistry");
            field.setAccessible(true);
            field.set(dao, DefaultServiceRegistry.INSTANCE);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
