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
 ******************************************************************************/

package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.collectd.Collectd.SchedulingCompletedFlag;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.persistence.rrd.RrdPersisterFactory;
import org.opennms.netmgt.collection.support.builder.AttributeType;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.test.FileAnticipator;
import org.springframework.transaction.PlatformTransactionManager;

public class CollectableServiceTest {

    private CollectionSpecification spec;
    private Scheduler scheduler;
    private CollectableService service;

    private File snmpDirectory;
    private FileAnticipator fileAnticipator;
    private RrdStrategy<?, ?> rrdStrategy;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        rrdStrategy = new JRobinRrdStrategy();
        fileAnticipator = new FileAnticipator();

        MockEventIpcManager mockEventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(mockEventIpcManager);
    }

    @After
    public void tearDown() {
        System.clearProperty(CollectableService.STRICT_INTERVAL_SYS_PROP);
        System.clearProperty(CollectableService.USE_COLLECTION_START_TIME_SYS_PROP);

        MockLogAppender.assertNoErrorOrGreater();
        fileAnticipator.deleteExpected();
        fileAnticipator.tearDown();
    }

    /**
     * Verifies that CollectableService is rescheduled according
     * to the specification's interval when the "strict interval" property is disabled
     * (which is by default).
     */
    @Test
    public void collectUsingSpecifiedInterval() throws CollectionInitializationException, CollectionException, IOException {
        createCollectableService();

        Long serviceIntervalInMs = 300 * 1000L;
        Long collectionDelayInMs = 1 * 1000L;
        when(spec.getInterval()).thenReturn(serviceIntervalInMs);
        when(spec.collect(any())).then(new Answer<CollectionSet>() {
            @Override
            public CollectionSet answer(InvocationOnMock invocation) throws InterruptedException {
                Thread.sleep(collectionDelayInMs);
                return null;
            }
        });

        ArgumentCaptor<Long> intervalCaptor = ArgumentCaptor.forClass(Long.class);
        service.run();
        verify(scheduler, times(1)).schedule(intervalCaptor.capture(), any());
        assertEquals(serviceIntervalInMs, intervalCaptor.getValue());
    }

    /**
     * Verifies that collection time is subtracted from the interval
     * when then "strict interval" property is enabled.
     */
    @Test
    public void collectUsingStrictInterval() throws CollectionInitializationException, CollectionException, IOException {
        System.setProperty(CollectableService.STRICT_INTERVAL_SYS_PROP, Boolean.TRUE.toString());
        createCollectableService();

        Long serviceIntervalInMs = 2000L;
        Long collectionDelayInMs = 500L;
        when(spec.getInterval()).thenReturn(serviceIntervalInMs);
        when(spec.collect(any())).then(new Answer<CollectionSet>() {
            @Override
            public CollectionSet answer(InvocationOnMock invocation) throws InterruptedException {
                Thread.sleep(collectionDelayInMs);
                return null;
            }
        });

        // Run the CollectableService and verify that the collection time is
        // subtracted from the interval when the "strict interval" property is enabled
        ArgumentCaptor<Long> intervalCaptor = ArgumentCaptor.forClass(Long.class);
        service.run();
        verify(scheduler, times(1)).schedule(intervalCaptor.capture(), any());

        Long upperBound = serviceIntervalInMs - collectionDelayInMs;
        assertTrue(String.format("Expected the interval to be less than %d, but was %d",
                upperBound, intervalCaptor.getValue()), intervalCaptor.getValue() <= upperBound);

        when(spec.collect(any())).then(new Answer<CollectionSet>() {
            @Override
            public CollectionSet answer(InvocationOnMock invocation) throws InterruptedException {
                Thread.sleep(2 * serviceIntervalInMs);
                return null;
            }
        });

        service.run();
        verify(scheduler, times(2)).schedule(intervalCaptor.capture(), any());
        assertEquals(Long.valueOf(0), intervalCaptor.getValue());
    }

    /**
     * Validates that we can successfully wrap collection sets with a custom time-keeper,
     * allowing us to override the timestamp of the attributes within the collection set.
     */
    @Test
    public void canWrapResourcesWithTimekeeper() throws CollectionInitializationException, CollectionException, IOException, RrdException {
        System.setProperty(CollectableService.USE_COLLECTION_START_TIME_SYS_PROP, Boolean.TRUE.toString());
        createCollectableService();

        long collectionDelayInSecs = 2;
        when(spec.collect(any())).then(new Answer<CollectionSet>() {
            @Override
            public CollectionSet answer(InvocationOnMock invocation) throws InterruptedException {
                Thread.sleep(collectionDelayInSecs * 1000);
                CollectionAgent agent = (CollectionAgent)invocation.getArguments()[0];
                NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
                return new CollectionSetBuilder(agent)
                    .withNumericAttribute(nodeResource, "mibGroup", "myCounter", 1000, AttributeType.COUNTER)
                    .build();
            }
        });

        File nodeDir = fileAnticipator.expecting(getSnmpRrdDirectory(), "1");
        File jrbFile = fileAnticipator.expecting(nodeDir, "myCounter" + rrdStrategy.getDefaultFileExtension());
        fileAnticipator.expecting(nodeDir, "myCounter" + ".meta");

        long beforeInMs = System.currentTimeMillis();
        service.run();
        long afterInMs = System.currentTimeMillis();
        // Quick sanity check
        assertTrue(String.format("Delay was not succesfully applied (delay was %d).",
                beforeInMs - afterInMs), afterInMs - beforeInMs >= collectionDelayInSecs * 1000);

        // Verify the last update time match the start of the collection time
        RrdDb rrdDb = new RrdDb(jrbFile);
        long lastUpdateTimeInSecs = rrdDb.getLastUpdateTime();
        long beforeInSecs = Math.floorDiv(beforeInMs, 1000);
        long afterInSecs = Math.floorDiv(afterInMs, 1000) + 1;

        assertTrue("Last update was before the collector was invoked!",
                lastUpdateTimeInSecs >= beforeInSecs);
        assertTrue("Last update was too long after the collector was invoked!",
                lastUpdateTimeInSecs < (afterInSecs - (collectionDelayInSecs / 2d)));
    }

    private void createCollectableService() throws CollectionInitializationException, IOException {
        // Mock it all!
        OnmsIpInterface iface = mock(OnmsIpInterface.class, RETURNS_DEEP_STUBS);
        IpInterfaceDao ifaceDao = mock(IpInterfaceDao.class);
        spec = mock(CollectionSpecification.class);
        scheduler = mock(Scheduler.class);
        SchedulingCompletedFlag schedulingCompletedFlag = mock(SchedulingCompletedFlag.class);
        PlatformTransactionManager transMgr = mock(PlatformTransactionManager.class);
        RrdPersisterFactory persisterFactory = new RrdPersisterFactory();
        persisterFactory.setRrdStrategy(rrdStrategy);
        ResourceStorageDao resourceStorageDao = mock(ResourceStorageDao.class);

        // Disable thresholding
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("thresholding-enabled", Boolean.FALSE.toString());
        ServiceParameters params = new ServiceParameters(paramsMap);

        when(iface.getNode().getId()).thenReturn(1);
        when(spec.getServiceParameters()).thenReturn(params);
        when(spec.getRrdRepository(any())).thenReturn(createRrdRepository());
        when(ifaceDao.load(any())).thenReturn(iface);
        when(iface.getIpAddress()).thenReturn(InetAddrUtils.getLocalHostAddress());

        service = new CollectableService(iface, ifaceDao, spec, scheduler, schedulingCompletedFlag, transMgr, persisterFactory, resourceStorageDao);
    }

    private RrdRepository createRrdRepository() throws IOException {
        RrdRepository repository = new RrdRepository();
        repository.setRrdBaseDir(getSnmpRrdDirectory());
        repository.setHeartBeat(600);
        repository.setStep(300);
        repository.setRraList(Collections.singletonList("RRA:AVERAGE:0.5:1:100"));
        return repository;
    }

    private File getSnmpRrdDirectory() throws IOException {
        if (snmpDirectory == null) {
            snmpDirectory = fileAnticipator.tempDir("snmp");
        }
        return snmpDirectory;
    }
}
