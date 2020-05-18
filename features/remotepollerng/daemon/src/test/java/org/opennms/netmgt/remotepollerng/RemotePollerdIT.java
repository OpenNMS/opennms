/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.remotepollerng;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.client.rpc.LocationAwarePollerClientImpl;
import org.opennms.netmgt.poller.monitors.IcmpMonitor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-testRemotePollerDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-shared.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class RemotePollerdIT implements InitializingBean {
    private final static File POLLER_CONFIG_1 = new File("src/test/resources/poller-configuration-1.xml");
    private final static File POLLER_CONFIG_2 = new File("src/test/resources/poller-configuration-2.xml");

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private PersisterFactory persisterFactory;

    @Autowired
    private MockEventIpcManager eventIpcManager;

    private CollectionAgentFactory collectionAgentFactory;

    @Autowired
    private DatabasePopulator databasePopulator;

    private AnnotationBasedEventListenerAdapter annotationBasedEventListenerAdapter;

    private RemotePollerd remotePollerd;

    @Test
    @Transactional
    public void reportResultTest() {
        final OnmsMonitoredService onmsMonitoredService = this.databasePopulator.getMonitoredServiceDao().findAll().get(0);
        RemotePolledService remotePolledService = new RemotePolledService(onmsMonitoredService, new Package(), new Service(), new IcmpMonitor());
        Assert.assertEquals(0, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());

        final int nodeId = onmsMonitoredService.getNodeId();
        final InetAddress ipAddress = onmsMonitoredService.getIpInterface().getIpAddress();
        final String service = onmsMonitoredService.getServiceName();
        final String location = onmsMonitoredService.getIpInterface().getNode().getLocation().getLocationName();

        this.eventIpcManager.getEventAnticipator().reset();
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, "RemotePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(service).setParam("location", location).getEvent());
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.available());
        Assert.assertEquals(1, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.available());
        Assert.assertEquals(1, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, "RemotePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(service).setParam("location", location).getEvent());
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.unavailable("old reason"));
        Assert.assertEquals(2, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.unavailable("old reason"));
        Assert.assertEquals(2, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.unavailable()); // reason is null
        Assert.assertEquals(2, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, "RemotePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(service).setParam("location", location).getEvent());
        this.remotePollerd.reportResult("RDU", remotePolledService, PollStatus.unavailable("new reason"));
        Assert.assertEquals(3, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();
    }

    private void reloadRemotePollerd() {
        EventBuilder ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test");
        ebldr.addParam(EventConstants.PARM_DAEMON_NAME, RemotePollerd.NAME);
        this.eventIpcManager.sendNow(ebldr.getEvent());
    }

    private void changePollingPackages(final String locationName, final String ... packages) {
        final OnmsMonitoringLocation onmsMonitoringLocation = this.databasePopulator.getMonitoringLocationDao().get(locationName);
        onmsMonitoringLocation.setPollingPackageNames(Lists.newArrayList(packages));
        this.databasePopulator.getMonitoringLocationDao().update(onmsMonitoringLocation);
    }

    @Test
    public void testDaemonReload() throws Exception {
        // initial config, package foo1 with 3 services, bound to RDU
        Assert.assertEquals(25, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.anyGroup()).size());

        // new config, package foo1 with 2 services, package foo2 with 1 service, only foo1 bound to RDU
        PollerConfigFactory.setPollerConfigFile(POLLER_CONFIG_2);
        reloadRemotePollerd();
        Assert.assertEquals(19, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.anyGroup()).size());

        // same config, package foo1 and foo2 bound to RDU
        changePollingPackages("RDU", "foo1", "foo2");
        reloadRemotePollerd();
        Assert.assertEquals(25, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.anyGroup()).size());
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        this.databasePopulator.populateDatabase();

        PollerConfigFactory.setPollerConfigFile(POLLER_CONFIG_1);
        PollerConfigFactory.setInstance(new PollerConfigFactory(-1L, new FileInputStream(POLLER_CONFIG_1)));
        changePollingPackages("RDU", "foo1");

        this.remotePollerd = new RemotePollerd(
                this.sessionUtils,
                this.databasePopulator.getMonitoringLocationDao(),
                PollerConfigFactory.getInstance(),
                this.databasePopulator.getMonitoredServiceDao(),
                new LocationAwarePollerClientImpl(new MockRpcClientFactory()),
                this.databasePopulator.getLocationSpecificStatusDao(),
                this.collectionAgentFactory,
                this.persisterFactory,
                this.eventIpcManager
        );

        this.annotationBasedEventListenerAdapter = new AnnotationBasedEventListenerAdapter(this.remotePollerd, eventIpcManager);
        this.remotePollerd.start();
    }

    @After
    public void tearDown() throws Exception {
        this.remotePollerd.destroy();
    }

    @Override
    public void afterPropertiesSet() {
    }
}
