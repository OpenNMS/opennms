/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.perspectivepoller;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opennms.core.utils.InetAddressUtils.addr;
import static org.opennms.netmgt.events.api.EventConstants.PARM_APPLICATION_ID;
import static org.opennms.netmgt.events.api.EventConstants.PARM_APPLICATION_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.mock.MockEntityScopeProvider;
import org.opennms.core.rpc.mock.MockRpcClientFactory;
import org.opennms.core.rpc.utils.RpcTargetHelper;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.api.OverrideableThresholdingDao;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.DatabasePopulator;
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

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-testPerspectivePollerDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-shared.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-thresholding.xml",
        "classpath:/META-INF/opennms/applicationContext-testPostgresBlobStore.xml",
        "classpath:/META-INF/opennms/applicationContext-testThresholdingDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-utils.xml",
        "classpath:/META-INF/opennms/applicationContext-jceks-scv.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
public class PerspectivePollerdIT implements InitializingBean, TemporaryDatabaseAware<MockDatabase> {
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
    private OverrideableThresholdingDao thresholdingDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private OutageDao outageDao;

    private PerspectivePollerd perspectivePollerd;

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
            this.databasePopulator.getApplicationDao().save(this.app1);

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
            this.databasePopulator.getApplicationDao().save(this.app2);

            this.node1snmp.addApplication(this.app2);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node1snmp);
            this.node2snmp.addApplication(this.app2);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node2snmp);

            return null;
        });

        final LocationAwarePollerClientImpl locationAwarePollerClient = new LocationAwarePollerClientImpl(new MockRpcClientFactory());
        locationAwarePollerClient.setEntityScopeProvider(new MockEntityScopeProvider());
        locationAwarePollerClient.setRpcTargetHelper(new RpcTargetHelper());
        locationAwarePollerClient.afterPropertiesSet();

        System.setProperty(PerspectiveServiceTracker.REFRESH_RATE_LIMIT_PROPERTY, "5");

        final PerspectiveServiceTracker tracker = new PerspectiveServiceTracker(this.sessionUtils, this.databasePopulator.getApplicationDao());
        new AnnotationBasedEventListenerAdapter(tracker, eventIpcManager);

        this.perspectivePollerd = new PerspectivePollerd(
                this.sessionUtils,
                this.databasePopulator.getMonitoringLocationDao(),
                PollerConfigFactory.getInstance(),
                this.databasePopulator.getMonitoredServiceDao(),
                locationAwarePollerClient,
                this.databasePopulator.getApplicationDao(),
                this.collectionAgentFactory,
                this.persisterFactory,
                this.eventIpcManager,
                this.thresholdingService,
                this.eventDao,
                this.outageDao,
                new MockTracerRegistry(),
                tracker
        );
        new AnnotationBasedEventListenerAdapter(this.perspectivePollerd, eventIpcManager);
    }

    @After
    public void teardown() throws Exception {
        this.perspectivePollerd.destroy();
    }

    @Test
    public void reportResultTest() throws Exception {
        this.perspectivePollerd.start();

        final Package pkg = PollerConfigFactory.getInstance().getPackage("foo1");
        final Package.ServiceMatch serviceMatch = pkg.findService("ICMP").get();
        final ServiceMonitor svcMon = PollerConfigFactory.getInstance().getServiceMonitor("ICMP");

        final int nodeId = this.node1icmp.getNodeId();
        final InetAddress ipAddress = this.node1icmp.getIpAddress();
        final String location = this.node1icmp.getIpInterface().getNode().getLocation().getLocationName();

        final PerspectivePolledService perspectivePolledService = findPerspectivePolledService(this.node1icmp, "RDU");
        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));

        this.eventIpcManager.getEventAnticipator().reset();
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.PERSPECTIVE_NODE_REGAINED_SERVICE_UEI, "PerspectivePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.perspectivePollerd.reportResult(perspectivePolledService, PollStatus.available());
        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.perspectivePollerd.reportResult(perspectivePolledService, PollStatus.available());
        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.PERSPECTIVE_NODE_LOST_SERVICE_UEI, "PerspectivePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.OUTAGE_CREATED_EVENT_UEI, "PerspectivePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.perspectivePollerd.reportResult(perspectivePolledService, PollStatus.unavailable("old reason"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(notNullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.perspectivePollerd.reportResult(perspectivePolledService, PollStatus.unavailable("old reason"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(notNullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.perspectivePollerd.reportResult(perspectivePolledService, PollStatus.unavailable("new reason"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(notNullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        this.eventIpcManager.getEventAnticipator().reset();
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.PERSPECTIVE_NODE_REGAINED_SERVICE_UEI, "PerspectivePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, "PerspectivePollerd").setNodeid(nodeId).setInterface(ipAddress).setService(serviceMatch.service.getName()).setParam("location", location).getEvent());
        this.perspectivePollerd.reportResult(perspectivePolledService, PollStatus.available());
        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));
        this.eventIpcManager.getEventAnticipator().verifyAnticipated();
    }

    @Test
    public void testCloseOutageOnUnschedule() throws Exception {
        this.perspectivePollerd.start();

        final Package pkg = PollerConfigFactory.getInstance().getPackage("foo1");
        final Package.ServiceMatch serviceMatch = pkg.findService("ICMP").get();
        final ServiceMonitor svcMon = PollerConfigFactory.getInstance().getServiceMonitor("ICMP");

        final int nodeId = this.node1icmp.getNodeId();
        final InetAddress ipAddress = this.node1icmp.getIpAddress();
        final String location = this.node1icmp.getIpInterface().getNode().getLocation().getLocationName();

        final PerspectivePolledService perspectivePolledService = findPerspectivePolledService(this.node1icmp, "RDU");
        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));

        this.perspectivePollerd.reportResult(perspectivePolledService, PollStatus.unavailable("old reason"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(notNullValue()));

        this.databasePopulator.getApplicationDao().delete(this.app1);
        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.APPLICATION_DELETED_EVENT_UEI, "test")
                                                 .addParam(PARM_APPLICATION_ID, this.app1.getId())
                                                 .addParam(PARM_APPLICATION_NAME, this.app1.getName())
                                                 .getEvent());

        await().atMost(5, TimeUnit.SECONDS).until(() -> this.databasePopulator.getOutageDao().currentOutageForServiceFromPerspective(this.node1icmp, this.databasePopulator.getLocRDU()), is(nullValue()));
    }

    @Test
    public void testDaemonReload() throws Exception {
        this.perspectivePollerd.start();

        // Initial config, ICMP and SNMP bound to single package
        Assert.assertEquals(8, this.perspectivePollerd.scheduler.getJobKeys(GroupMatcher.anyGroup()).size());
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU").getPkg().getName(), is("foo1"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "Fulda").getPkg().getName(), is("foo1"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2icmp, "RDU").getPkg().getName(), is("foo1"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2icmp, "Fulda").getPkg().getName(), is("foo1"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1snmp, "RDU").getPkg().getName(), is("foo1"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2snmp, "RDU").getPkg().getName(), is("foo1"));

        // New config, package ICMP and SNMP bound to two different packages
        PollerConfigFactory.setPollerConfigFile(POLLER_CONFIG_2);
        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test")
                                                 .addParam(EventConstants.PARM_DAEMON_NAME, PerspectivePollerd.NAME)
                                                 .getEvent());

        await().atMost(5, TimeUnit.SECONDS).until(() -> this.perspectivePollerd.scheduler.getJobKeys(GroupMatcher.anyGroup()).size(), is(8));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU").getPkg().getName(), is("foo1"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "Fulda").getPkg().getName(), is("foo1"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2icmp, "RDU").getPkg().getName(), is("foo1"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2icmp, "Fulda").getPkg().getName(), is("foo1"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1snmp, "RDU").getPkg().getName(), is("foo2"));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2snmp, "RDU").getPkg().getName(), is("foo2"));
    }

    @Test
    public void testAddService() throws Exception {
        this.perspectivePollerd.start();

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
            this.databasePopulator.getApplicationDao().saveOrUpdate(this.app1);

            this.databasePopulator.getMonitoredServiceDao().flush();
            this.databasePopulator.getApplicationDao().flush();

            return null;
        });

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(service1, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(service1, "Fulda"), is(nullValue()));

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, "test")
                                                 .setNodeid(node1.getId())
                                                 .setInterface(iface1.getIpAddress())
                                                 .setService(service1.getServiceName())
                                                 .getEvent());

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(service1, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(service1, "Fulda"), is(notNullValue()));
    }

    @Test
    public void testRemoveService() throws Exception {
        this.perspectivePollerd.start();

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "Fulda"), is(notNullValue()));

        this.databasePopulator.getMonitoredServiceDao().delete(this.node1icmp);
        this.databasePopulator.getMonitoredServiceDao().flush();

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.SERVICE_DELETED_EVENT_UEI, "test")
                                                 .setNodeid(this.node1icmp.getNodeId())
                                                 .setInterface(this.node1icmp.getIpAddress())
                                                 .setService(this.node1icmp.getServiceName())
                                                 .getEvent());

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "Fulda"), is(nullValue()));

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1snmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1http, "RDU"), is(notNullValue()));
    }

    @Test
    public void testRemoveInterface() throws Exception {
        this.perspectivePollerd.start();

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "Fulda"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1snmp, "RDU"), is(notNullValue()));

        this.databasePopulator.getIpInterfaceDao().delete(this.node1icmp.getIpInterface());
        this.databasePopulator.getIpInterfaceDao().flush();

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.INTERFACE_DELETED_EVENT_UEI, "test")
                                                 .setNodeid(this.node1icmp.getNodeId())
                                                 .setInterface(this.node1icmp.getIpAddress())
                                                 .getEvent());

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "Fulda"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1snmp, "RDU"), is(nullValue()));

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1http, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1http, "Fulda"), is(notNullValue()));
    }

    @Test
    public void testRemoveNode() throws Exception {
        this.perspectivePollerd.start();

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "Fulda"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1snmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1http, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1http, "Fulda"), is(notNullValue()));

        this.databasePopulator.getNodeDao().delete(this.node1icmp.getIpInterface().getNode());
        this.databasePopulator.getNodeDao().flush();

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.NODE_DELETED_EVENT_UEI, "test")
                                                 .setNodeid(this.node1icmp.getNodeId())
                                                 .getEvent());

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "Fulda"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1snmp, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1http, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1http, "Fulda"), is(nullValue()));
    }

    @Test
    public void testApplicationAdded() throws Exception {
        this.perspectivePollerd.start();

        final OnmsMonitoredService node3icmp = this.databasePopulator.getNode3().getPrimaryInterface().getMonitoredServiceByServiceType("ICMP");
        final OnmsMonitoredService node4icmp = this.databasePopulator.getNode4().getPrimaryInterface().getMonitoredServiceByServiceType("ICMP");

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(node3icmp, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(node4icmp, "RDU"), is(nullValue()));

        final OnmsApplication app = new OnmsApplication();
        app.setName("App Test");
        app.addPerspectiveLocation(this.databasePopulator.getLocRDU());
        app.addMonitoredService(node3icmp);
        app.addMonitoredService(node4icmp);

        node3icmp.addApplication(app);
        node4icmp.addApplication(app);

        this.databasePopulator.getTransactionTemplate().execute(tx -> {
            this.databasePopulator.getApplicationDao().save(app);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(node3icmp);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(node4icmp);

            this.databasePopulator.getApplicationDao().flush();
            this.databasePopulator.getMonitoredServiceDao().flush();
            return null;
        });

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.APPLICATION_CREATED_EVENT_UEI, "test")
                                                 .addParam(PARM_APPLICATION_ID, app.getId())
                                                 .addParam(PARM_APPLICATION_NAME, app.getName())
                                                 .getEvent());

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(node3icmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(node4icmp, "RDU"), is(notNullValue()));
    }

    @Test
    public void testApplicationChanged() throws Exception {
        this.perspectivePollerd.start();

        final OnmsMonitoredService node3icmp = this.databasePopulator.getNode3().getPrimaryInterface().getMonitoredServiceByServiceType("ICMP");
        final OnmsMonitoredService node4icmp = this.databasePopulator.getNode4().getPrimaryInterface().getMonitoredServiceByServiceType("ICMP");

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2icmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(node3icmp, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(node4icmp, "RDU"), is(nullValue()));

        this.app1.addMonitoredService(node3icmp);
        node3icmp.addApplication(this.app1);

        this.app1.removeMonitoredService(this.node1icmp);
        this.node1icmp.removeApplication(this.app1);

        this.databasePopulator.getTransactionTemplate().execute(tx -> {
            this.databasePopulator.getApplicationDao().saveOrUpdate(this.app1);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node1icmp);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node2icmp);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(node3icmp);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(node4icmp);

            this.databasePopulator.getApplicationDao().flush();
            this.databasePopulator.getMonitoredServiceDao().flush();
            return null;
        });

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.APPLICATION_CHANGED_EVENT_UEI, "test")
                                                 .addParam(PARM_APPLICATION_ID, this.app1.getId())
                                                 .addParam(PARM_APPLICATION_NAME, this.app1.getName())
                                                 .getEvent());

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2icmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(node3icmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(node4icmp, "RDU"), is(nullValue()));
    }

    @Test
    public void testApplicationRemoved() throws Exception {
        this.perspectivePollerd.start();

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(notNullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2icmp, "RDU"), is(notNullValue()));

        this.databasePopulator.getTransactionTemplate().execute(tx -> {
            this.databasePopulator.getApplicationDao().delete(this.app1);
            this.databasePopulator.getApplicationDao().flush();
            return null;
        });

        this.eventIpcManager.sendNowSync(new EventBuilder(EventConstants.APPLICATION_DELETED_EVENT_UEI, "test")
                                                 .addParam(PARM_APPLICATION_ID, this.app1.getId())
                                                 .addParam(PARM_APPLICATION_NAME, this.app1.getName())
                                                 .getEvent());

        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node1icmp, "RDU"), is(nullValue()));
        await().atMost(5, TimeUnit.SECONDS).until(() -> findPerspectivePolledService(this.node2icmp, "RDU"), is(nullValue()));
    }

    @Test
    public void testPerspectivePollerThresholding() throws Exception {
        this.perspectivePollerd.start();

        // this will return 192.168.1.1 for each call for active IPs
        final FilterDao filterDao = mock(FilterDao.class);
        when(filterDao.getActiveIPAddressList(anyString())).thenReturn(Collections.singletonList(addr("192.168.1.1")));
        FilterDaoFactory.setInstance(filterDao);

        // load the thresholds.xml and thresd-configuration.xml configuration
        this.thresholdingDao.overrideConfig(getClass().getResourceAsStream("/thresholds.xml"));
        this.threshdDao.overrideConfig(getClass().getResourceAsStream("/threshd-configuration.xml"));

        final Package pkg = PollerConfigFactory.getInstance().getPackage("foo1");
        final Package.ServiceMatch serviceMatch = pkg.findService("ICMP").get();
        final ServiceMonitor svcMon = PollerConfigFactory.getInstance().getServiceMonitor("ICMP");

        final PerspectivePolledService perspectivePolledService = findPerspectivePolledService(this.node1icmp, "RDU");

        this.eventIpcManager.getEventAnticipator().anticipateEvent(new EventBuilder(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "PerspectivePollerd")
                                                                           .setNodeid(this.node1icmp.getNodeId())
                                                                           .setInterface(this.node1icmp.getIpAddress())
                                                                           .setService(this.node1icmp.getServiceName())
                                                                           .setParam("location", this.node1icmp.getIpInterface().getNode().getLocation().getLocationName())
                                                                           .getEvent());

        final PollStatus pollStatus = PollStatus.available();
        pollStatus.setProperty(PollStatus.PROPERTY_RESPONSE_TIME, 51);
        this.perspectivePollerd.persistResponseTimeData(perspectivePolledService, pollStatus);

        this.eventIpcManager.getEventAnticipator().verifyAnticipated();

        verify(filterDao, atLeastOnce()).flushActiveIpAddressListCache();
    }

    @Test
    public void testStartWithEmptyApp() throws Exception {
        this.databasePopulator.getTransactionTemplate().execute(tx -> {
            final OnmsApplication app = new OnmsApplication();
            app.setName("App Empty");
            this.databasePopulator.getApplicationDao().save(app);

            return null;
        });

        this.perspectivePollerd.start();
    }

    @Test
    public void testStartWithLocationOnlyApp() throws Exception {
        this.databasePopulator.getTransactionTemplate().execute(tx -> {
            final OnmsApplication app = new OnmsApplication();
            app.setName("App Empty");
            app.addPerspectiveLocation(this.databasePopulator.getLocRDU());
            this.databasePopulator.getApplicationDao().save(app);

            return null;
        });

        this.perspectivePollerd.start();
    }

    @Test
    public void testStartWithServiceOnlyApp() throws Exception {
        this.databasePopulator.getTransactionTemplate().execute(tx -> {
            final OnmsApplication app = new OnmsApplication();
            app.setName("App Empty");
            app.addMonitoredService(this.node1icmp);
            this.databasePopulator.getApplicationDao().save(app);

            this.node1icmp.addApplication(app);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node1icmp);

            return null;
        });

        this.perspectivePollerd.start();
    }

    @Test
    public void testStartWithDuplicatedService() throws Exception {
        this.databasePopulator.getTransactionTemplate().execute(tx -> {
            final OnmsApplication appA = new OnmsApplication();
            appA.setName("App A");
            appA.addPerspectiveLocation(this.databasePopulator.getLocRDU());
            appA.addMonitoredService(this.node1icmp);
            this.databasePopulator.getApplicationDao().saveOrUpdate(appA);

            this.node1icmp.addApplication(appA);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node1icmp);

            final OnmsApplication appB = new OnmsApplication();
            appB.setName("App B");
            appB.addPerspectiveLocation(this.databasePopulator.getLocRDU());
            appB.addMonitoredService(this.node1icmp);
            this.databasePopulator.getApplicationDao().saveOrUpdate(appB);

            this.node1icmp.addApplication(appB);
            this.databasePopulator.getMonitoredServiceDao().saveOrUpdate(this.node1icmp);

            return null;
        });

        this.perspectivePollerd.start();
    }

    private PerspectivePolledService findPerspectivePolledService(final OnmsMonitoredService monSvc, final String locationName) throws Exception {
        return this.findPerspectivePolledService(monSvc.getNodeId(), monSvc.getIpAddress(), monSvc.getServiceName(), locationName);
    }

    private PerspectivePolledService findPerspectivePolledService(final int nodeId, final InetAddress ipAddress, final String serviceName, final String locationName) throws Exception {
        final JobKey jobKey = PerspectivePollerd.buildJobKey(nodeId, ipAddress, serviceName, locationName);

        final JobDetail jobDetail = this.perspectivePollerd.scheduler.getJobDetail(jobKey);
        if (jobDetail == null) {
            return null;
        }

        return (PerspectivePolledService) jobDetail.getJobDataMap().get(PerspectivePollJob.SERVICE);
    }

    @Override
    public void afterPropertiesSet() {
    }

    private static class MockTracerRegistry implements TracerRegistry {
        @Override
        public Tracer getTracer() {
            return GlobalTracer.get();
        }

        @Override
        public void init(String serviceName) {
        }
    }
}
