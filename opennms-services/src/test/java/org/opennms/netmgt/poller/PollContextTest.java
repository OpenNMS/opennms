/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableNetwork;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollContextTest 
 *
 * @author brozow
 */
public class PollContextTest {

    private MockNetwork m_mNetwork;
    private MockDatabase m_db;
    private MockPollerConfig m_pollerConfig;
    private DefaultPollContext m_pollContext;
    private PollableNetwork m_pNetwork;
    private PollableService m_pSvc;
    private MockService m_mSvc;
    private EventAnticipator m_anticipator;
    private OutageAnticipator m_outageAnticipator;
    private MockEventIpcManager m_eventMgr;

    @Before
    public void setUp() throws Exception {
        
        MockLogAppender.setupLogging();
        
        m_mNetwork = new MockNetwork();
        m_mNetwork.addNode(1, "Router");
        m_mNetwork.addInterface("192.168.1.1");
        m_mNetwork.addService("ICMP");
        m_mNetwork.addService("SMTP");
        m_mNetwork.addInterface("192.168.1.2");
        m_mNetwork.addService("ICMP");
        m_mNetwork.addService("SMTP");
        m_mNetwork.addNode(2, "Server");
        m_mNetwork.addInterface("192.168.1.3");
        m_mNetwork.addService("ICMP");
        m_mNetwork.addService("HTTP");
        m_mNetwork.addNode(3, "Firewall");
        m_mNetwork.addInterface("192.168.1.4");
        m_mNetwork.addService("SMTP");
        m_mNetwork.addService("HTTP");
        m_mNetwork.addInterface("192.168.1.5");
        m_mNetwork.addService("SMTP");
        m_mNetwork.addService("HTTP");
        
        m_mSvc = m_mNetwork.getService(1, "192.168.1.1", "ICMP");

        m_db = new MockDatabase();
        m_db.populate(m_mNetwork);
        
        DefaultQueryManager qm = new DefaultQueryManager();
        qm.setDataSource(m_db);
        
        m_pollerConfig = new MockPollerConfig(m_mNetwork);
        m_pollerConfig.setNodeOutageProcessingEnabled(true);
        m_pollerConfig.setCriticalService("ICMP");
        m_pollerConfig.addPackage("TestPackage");
        m_pollerConfig.addDowntime(1000L, 0L, -1L, false);
        m_pollerConfig.setDefaultPollInterval(1000L);
        m_pollerConfig.populatePackage(m_mNetwork);
        m_pollerConfig.addPackage("TestPkg2");
        m_pollerConfig.addDowntime(1000L, 0L, -1L, false);
        m_pollerConfig.setDefaultPollInterval(2000L);
        m_pollerConfig.addService(m_mNetwork.getService(2, "192.168.1.3", "HTTP"));
        m_pollerConfig.setNextOutageIdSql(m_db.getNextOutageIdStatement());
        
        m_anticipator = new EventAnticipator();
        m_outageAnticipator = new OutageAnticipator(m_db);
        
        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);
        
        m_pollContext = new DefaultPollContext();
        m_pollContext.setEventManager(m_eventMgr);
        m_pollContext.setLocalHostName("localhost");
        m_pollContext.setName("PollContextTest.DefaultPollContext");
        m_pollContext.setPollerConfig(m_pollerConfig);
        m_pollContext.setQueryManager(qm);
        
       m_pNetwork = new PollableNetwork(m_pollContext);
       m_pSvc = m_pNetwork.createService(1, "Router", InetAddressUtils.addr("192.168.1.1"), "ICMP");

    }

    @Test
    public void testGetCriticalServiceName() {
        assertEquals("ICMP", m_pollContext.getCriticalServiceName());
        
        m_pollerConfig.setCriticalService("HTTP");
        
        assertEquals("HTTP", m_pollContext.getCriticalServiceName());
    }

    @Test
    public void testIsNodeProcessingEnabled() {
        assertTrue(m_pollContext.isNodeProcessingEnabled());
        
        m_pollerConfig.setNodeOutageProcessingEnabled(false);
        
        assertFalse(m_pollContext.isNodeProcessingEnabled());
        
    }

    @Test
    public void testIsPollingAllIfCritServiceUndefined() {
        assertTrue(m_pollContext.isPollingAllIfCritServiceUndefined());
        
        m_pollerConfig.setPollAllIfNoCriticalServiceDefined(false);
        
        assertFalse(m_pollContext.isPollingAllIfCritServiceUndefined());
    }

    @Test
    public void testSendEvent() {
       
        m_anticipator.anticipateEvent(m_mSvc.createDownEvent());
        
        PollEvent e = m_pollContext.sendEvent(m_mSvc.createDownEvent());
        
        m_eventMgr.finishProcessingEvents();
        assertNotNull(e);
        assertTrue("Invalid Event Id", e.getEventId() > 0);
        
        assertEquals(0, m_anticipator.waitForAnticipated(0).size());
        assertEquals(0, m_anticipator.unanticipatedEvents().size());
        
        
    }

    @Test
    public void testCreateEvent() throws Exception {
        Date date = new Date(1222222222000L);
        Event nodeEvent = m_pollContext.createEvent(EventConstants.NODE_DOWN_EVENT_UEI, 1, null, null, date, String.valueOf(PollStatus.SERVICE_UNAVAILABLE));
        assertEquals(EventConstants.NODE_DOWN_EVENT_UEI, nodeEvent.getUei());
        assertEquals(Long.valueOf(1), nodeEvent.getNodeid());
        assertNull(nodeEvent.getInterface());
        assertNull(nodeEvent.getService());
        assertEquals("Unexpected time for event",date.toString(), EventConstants.parseToDate(nodeEvent.getTime()).toString());
        
        Event ifEvent = m_pollContext.createEvent(EventConstants.INTERFACE_UP_EVENT_UEI, 1, InetAddressUtils.addr("192.168.1.1"), null, date, null);
        assertEquals(EventConstants.INTERFACE_UP_EVENT_UEI, ifEvent.getUei());
        assertEquals(Long.valueOf(1), ifEvent.getNodeid());
        assertEquals("192.168.1.1", ifEvent.getInterface());
        assertNull(ifEvent.getService());
        assertEquals("Unexpected time for event", date.toString(), EventConstants.parseToDate(ifEvent.getTime()).toString());
        
        Event svcEvent = m_pollContext.createEvent(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, 1, InetAddressUtils.addr("192.168.1.1"), "ICMP", date, null);
        assertEquals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI, svcEvent.getUei());
        assertEquals(Long.valueOf(1), svcEvent.getNodeid());
        assertEquals("192.168.1.1", svcEvent.getInterface());
        assertEquals("ICMP", svcEvent.getService());
        assertEquals("Unexpected time for event", date.toString(), EventConstants.parseToDate(svcEvent.getTime()).toString());
        
    }

    @Test
    public void testOpenResolveOutage() throws Exception {
        Event downEvent = m_mSvc.createDownEvent();
        m_outageAnticipator.anticipateOutageOpened(m_mSvc, downEvent);
        PollEvent pollDownEvent = m_pollContext.sendEvent(downEvent);
        m_pollContext.openOutage(m_pSvc, pollDownEvent);
                                                  
        verifyOutages();
        
        m_outageAnticipator.reset();
        Event upEvent = m_mSvc.createUpEvent();
        m_outageAnticipator.anticipateOutageClosed(m_mSvc, upEvent);
        PollEvent pollUpEvent = m_pollContext.sendEvent(upEvent);
        m_pollContext.resolveOutage(m_pSvc, pollUpEvent);
                                   
        verifyOutages();

        // doing this a second time to ensure the database doesn't hose the outages
        // this was added to detect an actual bug
        
        Event downEvent2 = m_mSvc.createDownEvent();
        m_outageAnticipator.anticipateOutageOpened(m_mSvc, downEvent2);
        PollEvent pollDownEvent2 = m_pollContext.sendEvent(downEvent2);
        m_pollContext.openOutage(m_pSvc, pollDownEvent2);
                                                  
        verifyOutages();
        
        m_outageAnticipator.reset();
        Event upEvent2 = m_mSvc.createUpEvent();
        m_outageAnticipator.anticipateOutageClosed(m_mSvc, upEvent2);
        PollEvent pollUpEvent2 = m_pollContext.sendEvent(upEvent2);
        m_pollContext.resolveOutage(m_pSvc, pollUpEvent2);
                                   
        verifyOutages();
        
        
    }

    /**
     * 
     */
    private void verifyOutages() {
        m_eventMgr.finishProcessingEvents();
        assertEquals("Wrong number of outages opened", m_outageAnticipator.getExpectedOpens(), m_outageAnticipator.getActualOpens());
        assertEquals("Wrong number of outages in outage table", m_outageAnticipator.getExpectedOutages(), m_outageAnticipator.getActualOutages());
        assertTrue("Created outages don't match the expected outages", m_outageAnticipator.checkAnticipated());
    }

    @Test
    public void testIsServiceUnresponsiveEnabled() {
        assertFalse(m_pollContext.isServiceUnresponsiveEnabled());
        
        m_pollerConfig.setServiceUnresponsiveEnabled(true);
        
        assertTrue(m_pollContext.isServiceUnresponsiveEnabled());
    }

}
