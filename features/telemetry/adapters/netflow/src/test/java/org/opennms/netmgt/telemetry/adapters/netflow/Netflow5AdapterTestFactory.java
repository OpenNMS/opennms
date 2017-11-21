/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.netflow;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mockito;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.dao.mock.AbstractMockDao;
import org.opennms.netmgt.dao.mock.MockAssetRecordDao;
import org.opennms.netmgt.dao.mock.MockInterfaceToNodeCache;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.adapters.api.AdapterFactory;
import org.opennms.netmgt.telemetry.config.api.Protocol;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;

// Was a Spring configuration before, but now it is a simple factory class for test purposes
public class Netflow5AdapterTestFactory implements AdapterFactory {

    private final AtomicInteger nodeDaoGetCounter = new AtomicInteger(0);
    private final NodeDao nodeDao;
    private final InterfaceToNodeCache interfaceToNodeCache;
    private final MockAssetRecordDao assetRecordDao;

    private Netflow5AdapterFactory delegateFactory;

    public Netflow5AdapterTestFactory() {
        this.nodeDao = createNodeDao();
        this.interfaceToNodeCache = createInterfaceToNodeCache();
        this.assetRecordDao = new MockAssetRecordDao();

        // Required for mock node dao
        addServiceRegistry(nodeDao);
        addServiceRegistry(assetRecordDao);
        DefaultServiceRegistry.INSTANCE.register(nodeDao, NodeDao.class);
        DefaultServiceRegistry.INSTANCE.register(assetRecordDao, AssetRecordDao.class);
    }

    @Override
    public Class<? extends Adapter> getAdapterClass() {
        return Netflow5Adapter.class;
    }

    @Override
    public Adapter createAdapter(Protocol protocol, Map<String, String> properties) {
        if (delegateFactory == null) {
            delegateFactory = new Netflow5AdapterFactory();
            delegateFactory.setTransactionOperations(createTransactionOperations());
            delegateFactory.setNodeDao(nodeDao);
            delegateFactory.setMetricRegistry(createMetricRegistry());
            delegateFactory.setInterfaceToNodeCache(interfaceToNodeCache);
            delegateFactory.setFlowRepository(createFlowRepository());
        }
        return delegateFactory.createAdapter(protocol, properties);
    }

    public Netflow5Adapter createAdapter() {
        Adapter adapter = createAdapter(Mockito.mock(Protocol.class), new HashMap<>());
        return (Netflow5Adapter) adapter;
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public InterfaceToNodeCache getInterfaceToNodeCache() {
        return interfaceToNodeCache;
    }

    public AtomicInteger getNodeDaoGetCounter() {
        return nodeDaoGetCounter;
    }

    private InterfaceToNodeCache createInterfaceToNodeCache() {
        return new MockInterfaceToNodeCache();
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

    private TransactionOperations createTransactionOperations() {
        return new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> transactionCallback) throws TransactionException {
                return transactionCallback.doInTransaction(null);
            }
        };
    }

    private FlowRepository createFlowRepository() {
        return new FlowRepository() {
            @Override
            public void save(List<NetflowDocument> document) throws FlowException {

            }

            @Override
            public List<NetflowDocument> findAll(String query) throws FlowException {
                return null;
            }

            @Override
            public String rawQuery(String query) throws FlowException {
                return null;
            }
        };
    }

    private ServiceRegistry createServiceRegistry() {
        return DefaultServiceRegistry.INSTANCE;
    }

    private MetricRegistry createMetricRegistry() {
        return new MetricRegistry();
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
