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

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.rpc.mock.MockEntityScopeProvider;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.InsufficientInformationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.DefaultServiceCollectorRegistry;
import org.opennms.netmgt.collection.test.api.CollectorTestUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Filter;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.netmgt.config.dao.outages.api.OverrideablePollOutagesDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockPersisterFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * CollectdMoreIT
 *
 * @author brozow
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass = MockDatabase.class, reuseDatabase = false)
public class CollectdExternalIT implements TemporaryDatabaseAware<MockDatabase> {
    private EventIpcManager eventIpcManager;

    private Collectd collectd;

    private CollectdConfigFactory collectdConfigFactory;

//    private MockNetwork network = new MockNetwork();

    private MockDatabase db;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private IpInterfaceDao ipIfaceDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    @Autowired
    private MonitoringLocationDao monitoringLocationDao;

    @Autowired
    private ServiceTypeDao serviceTypeDao;

    @Autowired
    private FilterDao filterDao;

    @Autowired
    private OverrideablePollOutagesDao pollOutagesDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Before
    public void setUp() throws Exception {
        this.transactionTemplate.execute((tx) -> {
            final var snmpServiceType = new OnmsServiceType("SNMP");
            final var httpServiceType = new OnmsServiceType("HTTP");
            final var customServiceType = new OnmsServiceType("Custom");

            this.serviceTypeDao.saveOrUpdate(snmpServiceType);
            this.serviceTypeDao.saveOrUpdate(httpServiceType);
            this.serviceTypeDao.saveOrUpdate(customServiceType);

            final var node = new OnmsNode();
            node.setLabel("Example");
            node.setLocation(this.monitoringLocationDao.getDefaultLocation());
            this.nodeDao.saveOrUpdate(node);

            final var iface = new OnmsIpInterface();
            iface.setNode(node);
            iface.setIpAddress(InetAddressUtils.addr("192.168.1.1"));
            node.addIpInterface(iface);
            this.ipIfaceDao.saveOrUpdate(iface);

            final var svcSNMP = new OnmsMonitoredService();
            svcSNMP.setIpInterface(iface);
            svcSNMP.setServiceType(snmpServiceType);
            iface.addMonitoredService(svcSNMP);
            this.monitoredServiceDao.saveOrUpdate(svcSNMP);

            final var svcHTTP = new OnmsMonitoredService();
            svcHTTP.setIpInterface(iface);
            svcHTTP.setServiceType(httpServiceType);
            iface.addMonitoredService(svcHTTP);
            this.monitoredServiceDao.saveOrUpdate(svcHTTP);

            final var svcCustom = new OnmsMonitoredService();
            svcCustom.setIpInterface(iface);
            svcCustom.setServiceType(customServiceType);
            iface.addMonitoredService(svcCustom);
            this.monitoredServiceDao.saveOrUpdate(svcCustom);

            return null;
        });

        DataSourceFactory.setInstance(this.db);

        this.filterDao.flushActiveIpAddressListCache();
        FilterDaoFactory.setInstance(this.filterDao);

        this.eventIpcManager = new MockEventIpcManager();
        EventIpcManagerFactory.setIpcManager(this.eventIpcManager);

        Resource resource = new ClassPathResource("etc/poll-outages.xml");
        pollOutagesDao.overrideConfig(resource.getInputStream());

        File homeDir = resource.getFile().getParentFile().getParentFile();
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        this.collectdConfigFactory = new CollectdConfigFactory();

        this.collectd = new Collectd() {
            @Override
            protected void handleInsufficientInfo(InsufficientInformationException e) {
                fail("Invalid event received: " + e.getMessage());
            }
        };

        this.collectd.setPollOutagesDao(this.pollOutagesDao);
        this.collectd.setEntityScopeProvider(new MockEntityScopeProvider());

        ThresholdingService mockThresholdingService = mock(ThresholdingService.class);
        ThresholdingSession mockThresholdingSession = mock(ThresholdingSession.class);
        when(mockThresholdingService.createSession(anyInt(), anyString(), anyString(), any(ServiceParameters.class))).thenReturn(mockThresholdingSession);
        this.collectd.setThresholdingService(mockThresholdingService);

        mockThresholdingSession.accept(any(CollectionSet.class));

        this.collectd.setCollectdConfigFactory(this.collectdConfigFactory);
        this.collectd.setEventIpcManager(this.eventIpcManager);
        this.collectd.setTransactionTemplate(this.transactionTemplate);
        this.collectd.setIpInterfaceDao(this.ipIfaceDao);
        this.collectd.setNodeDao(this.nodeDao);
        this.collectd.setFilterDao(this.filterDao);
        this.collectd.setPersisterFactory(new MockPersisterFactory());
        this.collectd.setServiceCollectorRegistry(new DefaultServiceCollectorRegistry());
        this.collectd.setLocationAwareCollectorClient(CollectorTestUtils.createLocationAwareCollectorClient());

        // Inits the class
        this.collectd.init();
        this.collectd.start();
    }

    @After
    public void tearDown() throws Exception {
        this.collectd.getScheduler().stop();

        this.collectd.stop();

        this.db.drop();
    }

    @Test
    public void testSettingExternalData() throws Exception {
        // 2 of 3 services are collectable
        await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS)
                .until(this.collectd::getCollectableServiceCount, is(equalTo(2L)));

        final var svc = new Service();
        svc.setName("Custom");
        svc.setInterval(500000L);
        svc.setStatus("on");

        final var pkg = new Package();
        pkg.setName("test1");
        pkg.setFilter(new Filter("IPADDR != '0.0.0.0'"));
        pkg.addService(svc);

        final var col = new Collector();
        col.setService("Custom");
        col.setClassName("org.opennms.netmgt.collectd.MockServiceCollector");

        this.collectdConfigFactory.setExternalData(
                Collections.singletonList(pkg),
                Collections.singletonList(col));

        this.eventIpcManager.sendNow(
                new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test")
                        .addParam(EventConstants.PARM_DAEMON_NAME, "collectd")
                        .getEvent());

        // All 3 services are collectable
        await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS)
               .until(this.collectd::getCollectableServiceCount, is(equalTo(3L)));


        this.eventIpcManager.sendNow(
                new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test")
                        .addParam(EventConstants.PARM_DAEMON_NAME, "collectd")
                        .getEvent());

        // Still 3 services are collectable
        await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS)
                .until(this.collectd::getCollectableServiceCount, is(equalTo(3L)));

        this.collectdConfigFactory.setExternalData(
                Collections.emptyList(),
                Collections.emptyList());

        this.eventIpcManager.sendNow(
                new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "test")
                        .addParam(EventConstants.PARM_DAEMON_NAME, "collectd")
                        .getEvent());

        // Only 2 out of 3 as externals are gone
        await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS)
                .until(this.collectd::getCollectableServiceCount, is(equalTo(2L)));
    }

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        this.db = database;
    }
}
