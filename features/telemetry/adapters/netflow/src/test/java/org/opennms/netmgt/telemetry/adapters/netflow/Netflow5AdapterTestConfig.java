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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.mockito.Mockito;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockAssetRecordDao;
import org.opennms.netmgt.dao.mock.MockInterfaceToNodeCache;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowRepositoryProvider;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;

@Configuration
public class Netflow5AdapterTestConfig {

    private final AtomicInteger nodeDaoGetCounter = new AtomicInteger(0);

    @Autowired
    private ApplicationContext applicationContext;

    @Bean(name="flowAdapterMetricRegistry")
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public InterfaceToNodeCache getInterfaceToNodeCache() {
        return new MockInterfaceToNodeCache();
    }

    @Bean
    public NodeDao getNodeDao() {
        // Required by MockNodeDao.
        getServiceRegistry().register(new MockAssetRecordDao(), AssetRecordDao.class);

        // Spy on MockNodeDao to count access to get(int)
        final MockNodeDao dao = new MockNodeDao();
        final NodeDao spyMock = Mockito.spy(dao);
        Mockito.when(spyMock.get(Mockito.anyInt())).then(invocationOnMock -> {
            nodeDaoGetCounter.incrementAndGet();
            return dao.get((int) invocationOnMock.getArguments()[0]);
        });

        return spyMock;
    }

    @Bean
    public TransactionOperations getTransactionOperations() {
        return new TransactionOperations() {
            @Override
            public <T> T execute(TransactionCallback<T> transactionCallback) throws TransactionException {
                return transactionCallback.doInTransaction(null);
            }
        };
    }

    @Bean
    public FlowRepositoryProvider getFlowRepositoryProvider() {
        return () -> new FlowRepository() {
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

    @Bean
    public ServiceRegistry getServiceRegistry() {
        return DefaultServiceRegistry.INSTANCE;
    }

    @Bean
    public Netflow5Adapter getNetflow5Adapter() {
        Netflow5Adapter adapter = new Netflow5Adapter();
        AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
        autowireCapableBeanFactory.autowireBean(adapter);
        return adapter;
    }

    @Bean(name="nodeDaoGetCounter")
    public AtomicInteger getNodeDaoGetCounter() {
        return nodeDaoGetCounter;
    }
}
