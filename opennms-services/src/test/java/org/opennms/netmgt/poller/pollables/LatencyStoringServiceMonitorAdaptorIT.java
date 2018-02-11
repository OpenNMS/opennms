/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.pollables;

import static org.easymock.EasyMock.expect;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Rrd;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
public class LatencyStoringServiceMonitorAdaptorIT implements TemporaryDatabaseAware<MockDatabase> {
    private EasyMockUtils m_mocks;

    private PollerConfig m_pollerConfig;

    private MockDatabase m_db;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    private ApplicationContext m_context;

    @Autowired
    private PersisterFactory m_persisterFactory;

    @Autowired
    private ResourceStorageDao m_resourceStorageDao;

    @Override
    public void setTemporaryDatabase(MockDatabase database) {
        m_db = database;
    }

    private class MockServiceMonitor extends AbstractServiceMonitor {
        private  Double[] values;
        private int current = 0;

        public MockServiceMonitor(Double[] values) {
            this.values = values;
        }
        @Override
        public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
            return (PollStatus.get(PollStatus.SERVICE_AVAILABLE, values[current++]));
        }
    }

    @Before
    // Cannot avoid this warning since there is no way to fetch the class object for an interface
    // that uses generics
    public void setUp() throws Exception {
        BeanUtils.setStaticApplicationContext(m_context);

        m_mocks = new EasyMockUtils();
        m_pollerConfig = m_mocks.createMock(PollerConfig.class);

        MockLogAppender.setupLogging();

        String previousOpennmsHome = System.setProperty("opennms.home", "src/test/resources");
        PollOutagesConfigFactory.init();
        System.setProperty("opennms.home", previousOpennmsHome);

        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        network.addNode(1, "testNode");
        network.addInterface("127.0.0.1");
        network.setIfAlias("eth0");
        network.addService("ICMP");
        network.addService("SNMP");
        m_db.populate(network);
    }

    @After
    public void tearDown() throws Throwable {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public void testThresholds() throws Exception {
        EventBuilder bldr = new EventBuilder(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "LatencyStoringServiceMonitorAdaptorTest");
        bldr.setNodeid(1);
        bldr.setInterface(addr("127.0.0.1"));
        bldr.setService("ICMP");
        m_eventIpcManager.getEventAnticipator().anticipateEvent(bldr.getEvent());

        bldr = new EventBuilder(EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI, "LatencyStoringServiceMonitorAdaptorTest");
        bldr.setNodeid(1);
        bldr.setInterface(addr("127.0.0.1"));
        bldr.setService("ICMP");
        m_eventIpcManager.getEventAnticipator().anticipateEvent(bldr.getEvent());

        executeThresholdTest(new Double[] {100.0, 10.0}); // This should emulate a trigger and a rearm
        m_eventIpcManager.getEventAnticipator().verifyAnticipated();
    }

    // TODO: This test will fail if you have a default locale with >3 characters for month, e.g. Locale.FRENCH
    @Test
    @JUnitTemporaryDatabase(tempDbClass=MockDatabase.class)
    public void testThresholdsWithScheduledOutage() throws Exception {
        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        final StringBuilder sb = new StringBuilder("<?xml version=\"1.0\"?>");
        sb.append("<outages>");
        sb.append("<outage name=\"junit outage\" type=\"specific\">");
        sb.append("<time begins=\"");
        sb.append(formatter.format(new Date(System.currentTimeMillis() - 3600000)));
        sb.append("\" ends=\"");
        sb.append(formatter.format(new Date(System.currentTimeMillis() + 3600000)));
        sb.append("\"/>");
        sb.append("<interface address=\"match-any\"/>");
        sb.append("</outage>");
        sb.append("</outages>");

        File file = new File("target/poll-outages.xml");
        FileWriter writer = new FileWriter(file);
        writer.write(sb.toString());
        writer.close();

        PollOutagesConfigFactory oldFactory = PollOutagesConfigFactory.getInstance();
        PollOutagesConfigFactory.setInstance(new PollOutagesConfigFactory(new FileSystemResource(file)));
        PollOutagesConfigFactory.getInstance().afterPropertiesSet();

        executeThresholdTest(new Double[] {100.0});
        m_eventIpcManager.getEventAnticipator().verifyAnticipated();

        // Reset the state of the PollOutagesConfigFactory for any subsequent tests
        PollOutagesConfigFactory.setInstance(oldFactory);
        file.delete();
    }

    private void executeThresholdTest(Double[] rtValues) throws Exception {

        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("rrd-repository", "/tmp");
        parameters.put("ds-name", "icmp");
        parameters.put("rrd-base-name", "icmp");
        parameters.put("thresholding-enabled", "true");

        FilterDao filterDao = m_mocks.createMock(FilterDao.class);
        expect(filterDao.getActiveIPAddressList((String)EasyMock.anyObject())).andReturn(Collections.singletonList(addr("127.0.0.1"))).anyTimes();
        filterDao.flushActiveIpAddressListCache();
        EasyMock.expectLastCall().anyTimes();
        FilterDaoFactory.setInstance(filterDao);

        MonitoredService svc = m_mocks.createMock(MonitoredService.class);
        expect(svc.getNodeId()).andReturn(1);
        expect(svc.getIpAddr()).andReturn("127.0.0.1").atLeastOnce();
        expect(svc.getSvcName()).andReturn("ICMP").atLeastOnce();
        expect(svc.getNodeLocation()).andReturn(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID).atLeastOnce();

        ServiceMonitor service = new MockServiceMonitor(rtValues);

        int step = 1;
        List<String> rras = Collections.singletonList("RRA:AVERAGE:0.5:1:2016");
        Package pkg = new Package();
        Rrd rrd = new Rrd();
        rrd.setStep(step);
        rrd.setRras(rras);
        pkg.setRrd(rrd);

        expect(m_pollerConfig.getRRAList(pkg)).andReturn(rras).anyTimes();
        expect(m_pollerConfig.getStep(pkg)).andReturn(step).anyTimes();

        m_mocks.replayAll();
        LatencyStoringServiceMonitorAdaptor adaptor = new LatencyStoringServiceMonitorAdaptor(m_pollerConfig, pkg, m_persisterFactory, m_resourceStorageDao);
        // Make sure that the ThresholdingSet initializes with test settings
        String previousOpennmsHome = System.setProperty("opennms.home", "src/test/resources");
        for (int i=0; i<rtValues.length; i++) {
            adaptor.handlePollResult(svc, parameters, service.poll(svc, parameters));
            Thread.sleep(1000 * step); // Emulate the appropriate wait time prior inserting another value into the RRD files.
        }
        System.setProperty("opennms.home", previousOpennmsHome);
        m_mocks.verifyAll();
    }

}
