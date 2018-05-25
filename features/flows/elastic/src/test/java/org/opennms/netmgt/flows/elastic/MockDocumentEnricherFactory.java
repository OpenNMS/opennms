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

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mockito;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.dao.mock.MockAssetRecordDao;
import org.opennms.netmgt.dao.mock.MockCategoryDao;
import org.opennms.netmgt.dao.mock.MockInterfaceToNodeCache;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.internal.DefaultClassificationEngine;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;

public class MockDocumentEnricherFactory {

    private final NodeDao nodeDao;
    private final InterfaceToNodeCache interfaceToNodeCache;
    private final MockTransactionTemplate transactionTemplate;
    private final MockAssetRecordDao assetRecordDao;
    private final MockCategoryDao categoryDao;
    private final DocumentEnricher enricher;
    private final ClassificationEngine classificationEngine;

    private final AtomicInteger nodeDaoGetCounter = new AtomicInteger(0);

    public MockDocumentEnricherFactory() {
        nodeDao = createNodeDao();
        interfaceToNodeCache = new MockInterfaceToNodeCache();
        transactionTemplate = new MockTransactionTemplate();
        transactionTemplate.afterPropertiesSet();
        assetRecordDao = new MockAssetRecordDao();
        categoryDao = new MockCategoryDao();

        classificationEngine = new DefaultClassificationEngine(() -> Lists.newArrayList(
                new RuleBuilder().withName("http").withDstPort("80").withProtocol("tcp,udp").build(),
                new RuleBuilder().withName("https").withDstPort("443").withProtocol("tcp,udp").build()
        ), FilterService.NOOP);
        enricher = new DocumentEnricher(
                new MetricRegistry(), nodeDao, interfaceToNodeCache, transactionTemplate, classificationEngine,
                new CacheConfigBuilder()
                    .withName("flows.node")
                    .withMaximumSize(1000)
                    .withExpireAfterWrite(300)
                    .build());

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

    public MockTransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public DocumentEnricher getEnricher() {
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
