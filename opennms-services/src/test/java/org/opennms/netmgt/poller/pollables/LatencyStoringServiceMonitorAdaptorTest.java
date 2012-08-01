/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.pollables;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.endsWith;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.resource.Vault;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Rrd;
import org.opennms.netmgt.dao.support.NullRrdStrategy;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.mock.EventAnticipator;
import org.opennms.netmgt.eventd.mock.MockEventIpcManager;
import org.opennms.netmgt.filter.FilterDao;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.mock.EasyMockUtils;
import org.springframework.core.io.FileSystemResource;

public class LatencyStoringServiceMonitorAdaptorTest {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private PollerConfig m_pollerConfig = m_mocks.createMock(PollerConfig.class);

    // Cannot avoid this warning since there is no way to fetch the class object for an interface
    // that uses generics
    @SuppressWarnings("unchecked")
    private RrdStrategy<Object,Object> m_rrdStrategy = m_mocks.createMock(RrdStrategy.class);

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        RrdUtils.setStrategy(new NullRrdStrategy());
        RrdUtils.setStrategy(m_rrdStrategy);

        System.setProperty("opennms.home", "src/test/resources");
        PollOutagesConfigFactory.init();
    }

    @After
    public void tearDown() throws Throwable {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testUpdateRrdWithLocaleThatUsesCommasForDecimals() throws Exception {
        Locale defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.FRENCH);
        
        // Make sure we actually have a valid test
        NumberFormat nf = NumberFormat.getInstance();
        Assert.assertEquals("ensure that the newly set default locale (" + Locale.getDefault() + ") uses ',' as the decimal marker", "1,5", nf.format(1.5));
        
        LatencyStoringServiceMonitorAdaptor adaptor = new LatencyStoringServiceMonitorAdaptor(null, m_pollerConfig, new Package());
        LinkedHashMap<String, Number> map = new LinkedHashMap<String, Number>();
        map.put("cheese", 1.5);
        
        expect(m_pollerConfig.getStep(isA(Package.class))).andReturn(0).anyTimes();
        expect(m_pollerConfig.getRRAList(isA(Package.class))).andReturn(new ArrayList<String>(0));
        
        expect(m_rrdStrategy.getDefaultFileExtension()).andReturn(".rrd").anyTimes();
        expect(m_rrdStrategy.createDefinition(isA(String.class), isA(String.class), isA(String.class), anyInt(), isAList(RrdDataSource.class), isAList(String.class))).andReturn(new Object());
        m_rrdStrategy.createFile(isA(Object.class));
        expect(m_rrdStrategy.openFile(isA(String.class))).andReturn(new Object());
        m_rrdStrategy.updateFile(isA(Object.class), isA(String.class), endsWith(":1.5"));
        m_rrdStrategy.closeFile(isA(Object.class));
        
        m_mocks.replayAll();
        adaptor.updateRRD("foo", InetAddress.getLocalHost(), "baz", map);
        m_mocks.verifyAll();
        Locale.setDefault(defaultLocale);
    }
    
    @Test
    public void testThresholds() throws Exception {
        EventBuilder bldr = new EventBuilder(EventConstants.HIGH_THRESHOLD_EVENT_UEI, "LatencyStoringServiceMonitorAdaptorTest");
        bldr.setNodeid(1);
        bldr.setInterface(addr("127.0.0.1"));
        bldr.setService("ICMP");

        EventAnticipator anticipator = new EventAnticipator();
        anticipator.anticipateEvent(bldr.getEvent());
        executeThresholdTest(anticipator);
        anticipator.verifyAnticipated();
    }

    // TODO: This test will fail if you have a default locale with >3 characters for month, e.g. Locale.FRENCH
    @Test
    public void testThresholdsWithScheduledOutage() throws Exception {
        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        StringBuffer sb = new StringBuffer("<?xml version=\"1.0\"?>");
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
        PollOutagesConfigFactory.setInstance(new PollOutagesConfigFactory(new FileSystemResource(file)));
        PollOutagesConfigFactory.getInstance().afterPropertiesSet();
        
        EventAnticipator anticipator = new EventAnticipator();
        executeThresholdTest(anticipator);
        anticipator.verifyAnticipated();
    }

    private void executeThresholdTest(EventAnticipator anticipator) throws Exception {
        System.setProperty("opennms.home", "src/test/resources");
        
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("rrd-repository", "/tmp");
        parameters.put("ds-name", "icmp");
        parameters.put("rrd-base-name", "icmp");
        parameters.put("thresholding-enabled", "true");

        FilterDao filterDao = m_mocks.createMock(FilterDao.class);
        expect(filterDao.getActiveIPAddressList((String)EasyMock.anyObject())).andReturn(Collections.singletonList(addr("127.0.0.1"))).anyTimes();
        FilterDaoFactory.setInstance(filterDao);
        
        MonitoredService svc = m_mocks.createMock(MonitoredService.class);
        expect(svc.getNodeId()).andReturn(1);
        expect(svc.getIpAddr()).andReturn("127.0.0.1");
        expect(svc.getAddress()).andReturn(InetAddressUtils.addr("127.0.0.1"));
        expect(svc.getSvcName()).andReturn("ICMP");

        ServiceMonitor service = m_mocks.createMock(ServiceMonitor.class);
        PollStatus value = PollStatus.get(PollStatus.SERVICE_AVAILABLE, 100.0);
        expect(service.poll(svc, parameters)).andReturn(value);
        
        int step = 300;
        List<String> rras = Collections.singletonList("RRA:AVERAGE:0.5:1:2016");
        Package pkg = new Package();
        Rrd rrd = new Rrd();
        rrd.setStep(step);
        rrd.setRra(rras);
        pkg.setRrd(rrd);
        
        expect(m_pollerConfig.getRRAList(pkg)).andReturn(rras);
        expect(m_pollerConfig.getStep(pkg)).andReturn(step).anyTimes();
        
        expect(m_rrdStrategy.getDefaultFileExtension()).andReturn(".rrd").anyTimes();
        expect(m_rrdStrategy.createDefinition(isA(String.class), isA(String.class), isA(String.class), anyInt(), isAList(RrdDataSource.class), isAList(String.class))).andReturn(new Object());
        m_rrdStrategy.createFile(isA(Object.class));
        expect(m_rrdStrategy.openFile(isA(String.class))).andReturn(new Object());
        m_rrdStrategy.updateFile(isA(Object.class), isA(String.class), endsWith(":100"));
        m_rrdStrategy.closeFile(isA(Object.class));

        MockEventIpcManager eventMgr = new MockEventIpcManager();
        eventMgr.setEventAnticipator(anticipator);
        eventMgr.setSynchronous(true);
        EventIpcManager eventdIpcMgr = (EventIpcManager)eventMgr;
        EventIpcManagerFactory.setIpcManager(eventdIpcMgr);

        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        network.addNode(1, "testNode");
        network.addInterface("127.0.0.1");
        network.setIfAlias("eth0");
        network.addService("ICMP");
        network.addService("SNMP");
        MockDatabase db = new MockDatabase();
        db.populate(network);
        db.update("update snmpinterface set snmpifname=?, snmpifdescr=? where id=?", "eth0", "eth0", 1);
        DataSourceFactory.setInstance(db);
        Vault.setDataSource(db);
        
        m_mocks.replayAll();
        LatencyStoringServiceMonitorAdaptor adaptor = new LatencyStoringServiceMonitorAdaptor(service, m_pollerConfig, pkg);
        adaptor.poll(svc, parameters);
        m_mocks.verifyAll();
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> isAList(Class<T> clazz) {
        return isA(List.class);
    }
}
