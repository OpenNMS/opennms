/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.PersistenceSelectorStrategy;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.StrategyDefinition;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.collection.support.PersistAllSelectorStrategy;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.events.api.model.ImmutableEvent;
import org.opennms.netmgt.events.api.model.ImmutableParm;
import org.opennms.netmgt.events.api.model.ImmutableValue;
import org.opennms.netmgt.mock.MockPersister;
import org.opennms.netmgt.mock.MockResourceType;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-eventCollectorMock.xml",
})
@JUnitConfigurationEnvironment
public class EventMetricsCollectorIT {

    @Autowired
    private IpInterfaceDao ipInterfaceDao;

    @Autowired
    private SnmpInterfaceDao snmpInterfaceDao;

    /**
     * Create a collector with fake data and pass the persister into PersisterFactory
     *
     * @param persister
     * @return
     * @throws IOException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private EventMetricsCollector getCollector(Persister persister) throws IOException {
        // load testing eventconf
        DefaultEventConfDao eventConfDao = new DefaultEventConfDao();
        eventConfDao.setConfigResource(new FileSystemResource("src/test/resources/events/collection.events.xml"));
        eventConfDao.afterPropertiesSet();

        // fake interface info
        OnmsNode node = new OnmsNode();
        node.setId(1);
        OnmsIpInterface onmsIpInterface = new OnmsIpInterface();
        onmsIpInterface.setIpAddress(InetAddress.getByName("127.0.0.1"));
        onmsIpInterface.setId(1);
        onmsIpInterface.setNode(node);
        ipInterfaceDao.save(onmsIpInterface);

        EventSubscriptionService mockEventSubscriptionService = mock(EventSubscriptionService.class);
        ThresholdingService mockThresholdingService = mock(ThresholdingService.class, RETURNS_DEEP_STUBS);

        PersisterFactory persisterFactory = new PersisterFactory() {
            @Override
            public Persister createPersister(ServiceParameters params, RrdRepository repository) {
                return persister;
            }

            @Override
            public Persister createPersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters,
                                             boolean forceStoreByGroup, boolean dontReorderAttributes) {
                return persister;
            }
        };

        EventMetricsCollector collector = new EventMetricsCollector(eventConfDao, mockEventSubscriptionService,
                persisterFactory, ipInterfaceDao, snmpInterfaceDao, collectionAgentFactory, mockThresholdingService);

        return collector;
    }

    @Autowired
    private CollectionAgentFactory collectionAgentFactory;

    @Test
    public void testSingleParam() throws IOException {
        MockPersister persister = Mockito.mock(MockPersister.class);

        List<IParm> paramList = new ArrayList<>();
        IParm parm = ImmutableParm.newBuilder().setParmName(".1.3.6.1.4.1.22222.2.4.3.12.2.21").setValue(ImmutableValue.newBuilder().setContent("123").build()).build();
        paramList.add(parm);
        IEvent event = ImmutableEvent.newBuilder().setUei("uei.opennms.org/traps/test/varbind").setParms(paramList)
                .setInterfaceAddress(InetAddress.getByName("127.0.0.1")).setNodeid(1L).build();

        EventMetricsCollector collector = this.getCollector(persister);
        collector.onEvent(event);
        Mockito.verify(persister, Mockito.times(1)).visitAttribute(Mockito.any());
    }

    @Test
    public void testNameMatchingParam() throws IOException {
        MockPersister persister = Mockito.mock(MockPersister.class);
        ResourceTypeMapper.getInstance().setResourceTypeMapper(s -> {
            ResourceType rt = mock(ResourceType.class, RETURNS_DEEP_STUBS);
            when(rt.getName()).thenReturn("mock");
            when(rt.getStorageStrategy().getClazz()).thenReturn(IndexStorageStrategy.class.getCanonicalName());
            when(rt.getStorageStrategy().getParameters()).thenReturn(Collections.emptyList());
            when(rt.getPersistenceSelectorStrategy().getClazz()).thenReturn(PersistAllSelectorStrategy.class.getCanonicalName());
            when(rt.getPersistenceSelectorStrategy().getParameters()).thenReturn(Collections.emptyList());
            return rt;
        });

        List<IParm> paramList = new ArrayList<>();
        paramList.add(ImmutableParm.newBuilder().setParmName("TIME").setValue(ImmutableValue.newBuilder().setContent("100").build()).build());
        paramList.add(ImmutableParm.newBuilder().setParmName("VALUE").setValue(ImmutableValue.newBuilder().setContent("200").build()).build());
        paramList.add(ImmutableParm.newBuilder().setParmName("STATUS").setValue(ImmutableValue.newBuilder().setContent("primary").build()).build());
        paramList.add(ImmutableParm.newBuilder().setParmName("TAG").setValue(ImmutableValue.newBuilder().setContent("TAG").build()).build());
        // wrong param
        paramList.add(ImmutableParm.newBuilder().setParmName("WRONG").setValue(ImmutableValue.newBuilder().setContent("WRONG").build()).build());

        IEvent event = ImmutableEvent.newBuilder().setUei("uei.opennms.org/traps/test/regex").setParms(paramList)
                .setInterfaceAddress(InetAddress.getByName("127.0.0.1")).setNodeid(1L).build();

        EventMetricsCollector collector = Mockito.spy(this.getCollector(persister));
        collector.onEvent(event);

        // only 4 will be persisted
        Mockito.verify(persister, Mockito.times(4)).visitAttribute(Mockito.any());
    }
}