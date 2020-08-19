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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Collections;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.mock.MockEntityScopeProvider;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThresholdingDao;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.client.rpc.LocationAwarePollerClientImpl;
import org.opennms.netmgt.threshd.ThresholdingServiceImpl;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-testRemotePollerDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-shared.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/applicationContext-testPostgresBlobStore.xml",
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-utils.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
public class RemotePollerdIT implements InitializingBean, TemporaryDatabaseAware<MockDatabase> {
    private final static File POLLER_CONFIG_1 = new File("src/test/resources/poller-configuration-1.xml");
    private final static File POLLER_CONFIG_2 = new File("src/test/resources/poller-configuration-2.xml");

    @Autowired
    private SessionUtils sessionUtils;

    @Autowired
    private PersisterFactory persisterFactory;

    @Autowired
    private MockEventIpcManager eventIpcManager;

    @Autowired
    private CollectionAgentFactory collectionAgentFactory;

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private ThresholdingServiceImpl thresholdingService;

    @Autowired
    private OverrideableThreshdDao threshdDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private OverrideableThresholdingDao thresholdingDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private OutageDao outageDao;

    private RemotePollerd remotePollerd;

    private OnmsMonitoredService node1icmp;
    private OnmsMonitoredService node2icmp;
    private OnmsMonitoredService node1snmp;
    private OnmsMonitoredService node2snmp;
    private OnmsMonitoredService node1http;

    private OnmsApplication app1;
    private OnmsApplication app2;

    private MockDatabase database;

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        this.database = database;
    }

    @Before
    public void setUp() throws Exception {
        this.databasePopulator.populateDatabase();

        this.eventIpcManager.setEventWriter(this.database);

        PollerConfigFactory.setPollerConfigFile(POLLER_CONFIG_1);
        PollerConfigFactory.setInstance(new PollerConfigFactory(-1L, new FileInputStream(POLLER_CONFIG_1)));
//        changePollingPackages("RDU", "foo1");

        this.databasePopulator.getTransactionTemplate().execute(transactionStatus -> {
            this.node1icmp = this.databasePopulator.getNode1().getPrimaryInterface().getMonitoredServiceByServiceType("ICMP");
            this.node2icmp = this.databasePopulator.getNode2().getPrimaryInterface().getMonitoredServiceByServiceType("ICMP");
            this.node1snmp = this.databasePopulator.getNode1().getPrimaryInterface().getMonitoredServiceByServiceType("SNMP");
            this.node2snmp = this.databasePopulator.getNode2().getPrimaryInterface().getMonitoredServiceByServiceType("SNMP");
            this.node1http = this.databasePopulator.getNode1().getInterfaceWithService("HTTP").getMonitoredServiceByServiceType("HTTP");

            this.app1 = new OnmsApplication();
            this.app1.setName("App1");
            this.app1.addPerspectiveLocation(this.databasePopulator.getLocRDU());
            this.app1.addPerspectiveLocation(this.databasePopulator.getLocFD());
            this.app1.addMonitoredService(this.node1icmp);
            this.app1.addMonitoredService(this.node2icmp);
            this.app1.addMonitoredService(this.node1http);
            this.applicationDao.save(this.app1);

            this.node1icmp.addApplication(this.app1);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node1icmp);
            this.node2icmp.addApplication(this.app1);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node2icmp);
            this.node1http.addApplication(this.app1);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node1http);

            this.app2 = new OnmsApplication();
            this.app2.setName("App2");
            this.app2.addPerspectiveLocation(this.databasePopulator.getLocRDU());
            this.app2.addMonitoredService(this.node1snmp);
            this.app2.addMonitoredService(this.node2snmp);
            this.applicationDao.save(this.app2);

            this.node1snmp.addApplication(this.app2);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node1snmp);
            this.node2snmp.addApplication(this.app2);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node2snmp);

            return null;
        });

        final LocationAwarePollerClientImpl locationAwarePollerClient = new LocationAwarePollerClientImpl(new MockRpcClientFactory());
        locationAwarePollerClient.setEntityScopeProvider(new MockEntityScopeProvider());

        this.remotePollerd = new RemotePollerd(
                this.sessionUtils,
                this.databasePopulator.getMonitoringLocationDao(),
                PollerConfigFactory.getInstance(),
                this.databasePopulator.getMonitoredServiceDao(),
                locationAwarePollerClient,
                this.databasePopulator.getLocationSpecificStatusDao(),
                this.databasePopulator.getApplicationDao(),
                this.collectionAgentFactory,
                this.persisterFactory,
                this.eventIpcManager,
                this.thresholdingService,
                this.eventDao,
                this.outageDao
        );

        new AnnotationBasedEventListenerAdapter(this.remotePollerd, eventIpcManager);
        new AnnotationBasedEventListenerAdapter(this.remotePollerd.getServiceTracker(), eventIpcManager);

        this.remotePollerd.start();
    }

    @After
    public void teardown() throws Exception {
        this.remotePollerd.destroy();
        this.databasePopulator.resetDatabase();
    }

    private void sendReloadRemotePollerdEvent() {
        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test")
                                                 .addParam(EventConstants.PARM_DAEMON_NAME, RemotePollerd.NAME)
                                                 .getEvent());
    }

    @Test
    public void reportResultTest() throws Exception {
        final Package pkg = PollerConfigFactory.getInstance().getPackage("foo1");
        final Package.ServiceMatch serviceMatch = pkg.findService("ICMP").get();
        final ServiceMonitor svcMon = PollerConfigFactory.getInstance().getServiceMonitor("ICMP");

        final int nodeId = this.node1icmp.getNodeId();
        final InetAddress ipAddress = this.node1icmp.getIpAddress();
        final String location = this.node1icmp.getIpInterface().getNode().getLocation().getLocationName();

        final RemotePolledService remotePolledService = findRemotePolledService(this.node1icmp, "RDU");
        assertThat(this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));

        this.eventIpcManager.getEventAnticipator().reset();
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, "RemotePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.remotePollerd.reportResult(remotePolledService, PollStatus.available());
        assertThat(this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.remotePollerd.reportResult(remotePolledService, PollStatus.available());
        assertThat(this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.REMOTE_NODE_LOST_SERVICE_UEI, "RemotePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.OUTAGE_CREATED_EVENT_UEI, "RemotePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.remotePollerd.reportResult(remotePolledService, PollStatus.unavailable("old reason"));
        assertThat(this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(notNullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.remotePollerd.reportResult(remotePolledService, PollStatus.unavailable("old reason"));
        assertThat(this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(notNullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.remotePollerd.reportResult(remotePolledService, PollStatus.unavailable("new reason"));
        assertThat(this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(notNullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.REMOTE_NODE_REGAINED_SERVICE_UEI, "RemotePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, "RemotePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.remotePollerd.reportResult(remotePolledService, PollStatus.available());
        assertThat(this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();
    }

    @Test
    public void testDaemonReload() throws Exception {
        // Initial config, ICMP and SNMP bound to single package
        Assert.assertEquals(8, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.anyGroup()).size());
        assertThat(findRemotePolledService(this.node1icmp, "RDU").getPkg().getName(), is("foo1"));
        assertThat(findRemotePolledService(this.node1icmp, "Fulda").getPkg().getName(), is("foo1"));
        assertThat(findRemotePolledService(this.node2icmp, "RDU").getPkg().getName(), is("foo1"));
        assertThat(findRemotePolledService(this.node2icmp, "Fulda").getPkg().getName(), is("foo1"));
        assertThat(findRemotePolledService(this.node1snmp, "RDU").getPkg().getName(), is("foo1"));
        assertThat(findRemotePolledService(this.node2snmp, "RDU").getPkg().getName(), is("foo1"));

        // New config, package ICMP and SNMP bound to two different packages
        PollerConfigFactory.setPollerConfigFile(POLLER_CONFIG_2);
        sendReloadRemotePollerdEvent();
        Assert.assertEquals(6, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.anyGroup()).size());
        assertThat(findRemotePolledService(this.node1icmp, "RDU").getPkg().getName(), is("foo1"));
        assertThat(findRemotePolledService(this.node1icmp, "Fulda").getPkg().getName(), is("foo1"));
        assertThat(findRemotePolledService(this.node2icmp, "RDU").getPkg().getName(), is("foo1"));
        assertThat(findRemotePolledService(this.node2icmp, "Fulda").getPkg().getName(), is("foo1"));
    }

    @Test
    public void testAddService() throws Exception {
        final NetworkBuilder builder = new NetworkBuilder();

        // Add service with application
        final OnmsNode node1 = builder.addNode("node1")
                                      .setForeignSource("imported:")
                                      .setForeignId("23")
                                      .setType(OnmsNode.NodeType.ACTIVE)
                                      .getNode();
        final OnmsIpInterface iface1 = builder.addSnmpInterface(1)
                                              .setCollectionEnabled(true)
                                              .setIfOperStatus(1)
                                              .setIfSpeed(10000000)
                                              .setIfDescr("ATM0")
                                              .setIfAlias("Initial ifAlias value")
                                              .setIfType(37)
                                              .setPhysAddr("34E45604BB69")
                                              .addIpInterface("1.2.3.4")
                                              .setIsManaged("M")
                                              .setIsSnmpPrimary("P")
                                              .getInterface();
        final OnmsMonitoredService service1 = builder.addService(this.databasePopulator.getService("ICMP"));
        service1.addApplication(this.app1);

        this.app1.addMonitoredService(service1);

        this.databasePopulator.getTransactionTemplate().execute(tx -> {
            this.databasePopulator.getNodeDao().saveOrUpdate(node1);
            this.databasePopulator.getSnmpInterfaceDao().saveOrUpdate(iface1.getSnmpInterface());
            this.databasePopulator.getIpInterfaceDao().saveOrUpdate(iface1);
            this.databasePopulator.getServiceTypeDao().saveOrUpdate(service1.getServiceType());
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(service1);
            this.applicationDao.saveOrUpdate(this.app1);

            this.databasePopulator.getMonitoredServiceDao().flush();
            this.applicationDao.flush();

            return null;
        });

        assertThat(findRemotePolledService(service1, "RDU"), is(nullValue()));
        assertThat(findRemotePolledService(service1, "Fulda"), is(nullValue()));

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "test")
                                                 .setNodeid(node1.getId())
                                                 .setInterface(iface1.getIpAddress())
                                                 .setService(service1.getServiceName())
                                                 .getEvent());

        assertThat(findRemotePolledService(service1, "RDU"), is(notNullValue()));
        assertThat(findRemotePolledService(service1, "Fulda"), is(notNullValue()));
    }

    @Test
    public void testRemoveService() throws Exception {
        assertThat(findRemotePolledService(this.node1icmp, "RDU"), is(notNullValue()));
        assertThat(findRemotePolledService(this.node1icmp, "Fulda"), is(notNullValue()));

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.SERVICE_DELETED_EVENT_UEI, "test")
                                                 .setNodeid(this.node1icmp.getNodeId())
                                                 .setInterface(this.node1icmp.getIpAddress())
                                                 .setService(this.node1icmp.getServiceName())
                                                 .getEvent());

        assertThat(findRemotePolledService(this.node1icmp, "RDU"), is(nullValue()));
        assertThat(findRemotePolledService(this.node1icmp, "Fulda"), is(nullValue()));

        assertThat(findRemotePolledService(this.node1snmp, "RDU"), is(notNullValue()));
        assertThat(findRemotePolledService(this.node1http, "RDU"), is(notNullValue()));
    }

    @Test
    public void testRemoveInterface() throws Exception {
        assertThat(findRemotePolledService(this.node1icmp, "RDU"), is(notNullValue()));
        assertThat(findRemotePolledService(this.node1icmp, "Fulda"), is(notNullValue()));
        assertThat(findRemotePolledService(this.node1snmp, "RDU"), is(notNullValue()));

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.INTERFACE_DELETED_EVENT_UEI, "test")
                                                 .setNodeid(this.node1icmp.getNodeId())
                                                 .setInterface(this.node1icmp.getIpAddress())
                                                 .getEvent());

        assertThat(findRemotePolledService(this.node1icmp, "RDU"), is(nullValue()));
        assertThat(findRemotePolledService(this.node1icmp, "Fulda"), is(nullValue()));
        assertThat(findRemotePolledService(this.node1snmp, "RDU"), is(nullValue()));

        assertThat(findRemotePolledService(this.node1http, "RDU"), is(notNullValue()));
        assertThat(findRemotePolledService(this.node1http, "Fulda"), is(notNullValue()));
    }

    @Test
    public void testRemoveNode() throws Exception {
        assertThat(findRemotePolledService(this.node1icmp, "RDU"), is(notNullValue()));
        assertThat(findRemotePolledService(this.node1icmp, "Fulda"), is(notNullValue()));
        assertThat(findRemotePolledService(this.node1snmp, "RDU"), is(notNullValue()));
        assertThat(findRemotePolledService(this.node1http, "RDU"), is(notNullValue()));
        assertThat(findRemotePolledService(this.node1http, "Fulda"), is(notNullValue()));

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.NODE_DELETED_EVENT_UEI, "test")
                                                 .setNodeid(this.node1icmp.getNodeId())
                                                 .getEvent());

        assertThat(findRemotePolledService(this.node1icmp, "RDU"), is(nullValue()));
        assertThat(findRemotePolledService(this.node1icmp, "Fulda"), is(nullValue()));
        assertThat(findRemotePolledService(this.node1snmp, "RDU"), is(nullValue()));
        assertThat(findRemotePolledService(this.node1http, "RDU"), is(nullValue()));
        assertThat(findRemotePolledService(this.node1http, "Fulda"), is(nullValue()));
    }

    @Test
    @Ignore // TODO fooker: rewrite vor new events
    public void testDaemonReloadForLocation() throws Exception {
        final OnmsMonitoringLocation onmsMonitoringLocation = new OnmsMonitoringLocation();
        onmsMonitoringLocation.setLocationName("Fulda");
        onmsMonitoringLocation.setMonitoringArea("Fulda");
        onmsMonitoringLocation.setPriority(100L);
        // TODO: Patrick onmsMonitoringLocation.setPollingPackageNames(Lists.newArrayList("foo1", "foo2"));
        this.databasePopulator.getMonitoringLocationDao().save(onmsMonitoringLocation);
        this.databasePopulator.getMonitoringLocationDao().flush();

        PollerConfigFactory.setPollerConfigFile(POLLER_CONFIG_2);
//        changePollingPackages("RDU", "foo1", "foo2");
//        changePollingPackages("Fulda", "foo1", "foo2");
        sendReloadRemotePollerdEvent();

        // both locations have foo1 and foo2 assigned
        Assert.assertEquals(8, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.jobGroupEquals("RDU")).size());
        Assert.assertEquals(8, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.jobGroupEquals("Fulda")).size());

        // now remove foo1 from location Fulda and send event for location Fulda
//        changePollingPackages("Fulda", "foo2");
//        sendPollingPackageAssociationChanged("Fulda");
        Assert.assertEquals(8, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.jobGroupEquals("RDU")).size());
        Assert.assertEquals(2, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.jobGroupEquals("Fulda")).size());

        // now remove foo2 from location RDU but send an event for Fulda, so nothing will change
//        changePollingPackages("RDU", "foo1");
//        sendPollingPackageAssociationChanged("Fulda");
        Assert.assertEquals(8, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.jobGroupEquals("RDU")).size());
        Assert.assertEquals(2, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.jobGroupEquals("Fulda")).size());

        // now send event for RDU, changes will be applied
//        sendPollingPackageAssociationChanged("RDU");
        Assert.assertEquals(6, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.jobGroupEquals("RDU")).size());
        Assert.assertEquals(2, this.remotePollerd.scheduler.getJobKeys(GroupMatcher.jobGroupEquals("Fulda")).size());
    }

    @Test
    public void testRemotePollerThresholding() throws Exception {
        // this will return 192.168.1.1 for each call for active IPs
        final FilterDao filterDao = EasyMock.createMock(FilterDao.class);
        EasyMock.expect(filterDao.getActiveIPAddressList((String) EasyMock.anyObject())).andReturn(Collections.singletonList(addr("192.168.1.1"))).anyTimes();
        filterDao.flushActiveIpAddressListCache();
        EasyMock.expectLastCall().anyTimes();
        FilterDaoFactory.setInstance(filterDao);
        EasyMock.replay(filterDao);

        // load the thresholds.xml and thresd-configuration.xml configuration
        this.thresholdingDao.overrideConfig(getClass().getResourceAsStream("/thresholds.xml"));
        this.threshdDao.overrideConfig(getClass().getResourceAsStream("/threshd-configuration.xml"));

        final Package pkg = PollerConfigFactory.getInstance().getPackage("foo1");
        final Package.ServiceMatch serviceMatch = pkg.findService("ICMP").get();
        final ServiceMonitor svcMon = PollerConfigFactory.getInstance().getServiceMonitor("ICMP");

        final RemotePolledService remotePolledService = findRemotePolledService(this.node1icmp, "RDU");
        Assert.assertEquals(0, this.databasePopulator.getLocationSpecificStatusDao().findAll().size());

        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "RemotePollerd")
                                                                           .setNodeid(this.node1icmp.getNodeId())
                                                                           .setInterface(this.node1icmp.getIpAddress())
                                                                           .setService(this.node1icmp.getServiceName())
                                                                           .setParam("location", this.node1icmp.getIpInterface().getNode().getLocation().getLocationName())
                                                                           .getEvent());

        final PollStatus pollStatus = PollStatus.available();
        pollStatus.setProperty(PollStatus.PROPERTY_RESPONSE_TIME, 51);
        this.remotePollerd.persistResponseTimeData(remotePolledService, pollStatus);

        this.eventIpcManager.getEventAnticipator().verifyAnticipated();
    }

    private RemotePolledService findRemotePolledService(final OnmsMonitoredService monSvc, final String locationName) throws Exception {
        return this.findRemotePolledService(monSvc.getNodeId(), monSvc.getIpAddress(), monSvc.getServiceName(), locationName);
    }

    private RemotePolledService findRemotePolledService(final int nodeId, final InetAddress ipAddress, final String serviceName, final String locationName) throws Exception {
        final JobKey jobKey = RemotePollerd.buildJobKey(nodeId, ipAddress, serviceName, locationName);

        final JobDetail jobDetail = this.remotePollerd.scheduler.getJobDetail(jobKey);
        if (jobDetail == null) {
            return null;
        }

        return (RemotePolledService) jobDetail.getJobDataMap().get(RemotePollJob.POLLED_SERVICE);
    }

    @Override
    public void afterPropertiesSet() {
    }
}
