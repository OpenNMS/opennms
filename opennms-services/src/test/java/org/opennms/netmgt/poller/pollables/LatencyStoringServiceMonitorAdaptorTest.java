/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2008 Mar 03: Quiet test with MockLogAppender. - dj@opennms.org
 * 
 * Created April 30, 2008
 *
 * Copyright (C) 2008 Daniel J. Gregor, Jr..  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.poller.pollables;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.endsWith;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.net.InetAddress;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.test.mock.EasyMockUtils;
import org.opennms.test.mock.MockLogAppender;

import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Rrd;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.support.RrdTestUtils;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.rrd.RrdDataSource;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.xml.event.Event;

public class LatencyStoringServiceMonitorAdaptorTest {
    private EasyMockUtils m_mocks = new EasyMockUtils();
    private PollerConfig m_pollerConfig = m_mocks.createMock(PollerConfig.class);
    private RrdStrategy m_rrdStrategy = m_mocks.createMock(RrdStrategy.class);
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        RrdTestUtils.initializeNullStrategy();
        RrdUtils.setStrategy(m_rrdStrategy);
    }

    @After
    public void tearDown() throws Throwable {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testUpdateRrdWithLocaleThatUsesCommasForDecimals() throws Exception {
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
    }
    
    @Test
    public void testThresholds() throws Exception {
        System.setProperty("opennms.home", "src/test/resources");
        
        Map<String,Object> parameters = new HashMap<String,Object>();
        parameters.put("rrd-repository", "/tmp");
        parameters.put("ds-name", "icmp");
        parameters.put("rrd-base-name", "icmp");
        parameters.put("thresholding-enabled", "true");
        
        FilterDao filterDao = m_mocks.createMock(FilterDao.class);
        expect(filterDao.getIPList((String)EasyMock.anyObject())).andReturn(Collections.singletonList("127.0.0.1")).anyTimes();
        FilterDaoFactory.setInstance(filterDao);
        
        MonitoredService svc = m_mocks.createMock(MonitoredService.class);
        expect(svc.getNodeId()).andReturn(1);
        expect(svc.getIpAddr()).andReturn("127.0.0.1");
        expect(svc.getAddress()).andReturn(InetAddress.getByName("127.0.0.1"));
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

        EventAnticipator anticipator = new EventAnticipator();
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

        Event event = new Event();
        event.setUei("uei.opennms.org/threshold/highThresholdExceeded");
        event.setNodeid(1);
        event.setInterface("127.0.0.1");
        event.setService("ICMP");
        anticipator.anticipateEvent(event);
        m_mocks.replayAll();
        LatencyStoringServiceMonitorAdaptor adaptor = new LatencyStoringServiceMonitorAdaptor(service, m_pollerConfig, pkg);
        adaptor.poll(svc, parameters);
        m_mocks.verifyAll();
        anticipator.verifyAnticipated();
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> isAList(Class<T> clazz) {
        return isA(List.class);
    }
}
