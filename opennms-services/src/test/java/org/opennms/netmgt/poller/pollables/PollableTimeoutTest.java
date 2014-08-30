/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.Querier;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockElement;
import org.opennms.netmgt.mock.MockInterface;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.mock.MockPollerConfig;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockPollContext;
import org.opennms.netmgt.poller.mock.MockScheduler;
import org.opennms.netmgt.poller.mock.MockTimer;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.ScheduleTimer;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author roskens
 */
public class PollableTimeoutTest {
    private static final Logger LOG = LoggerFactory.getLogger(PollableTimeoutTest.class);

    private final long DEFAULT_INTERVAL = TimeUnit.SECONDS.toMillis(1);

    private PollableNetwork m_network;
    private MockPollContext m_pollContext;
    private MockNetwork m_mockNetwork;
    private MockDatabase m_db;
    private EventAnticipator m_anticipator;
    private MockEventIpcManager m_eventMgr;
    private MockNode mNode1;
    private MockInterface mDot1;
    private MockService mDot1Sleep;
    private MockService mDot1Icmp;

    private PollableNode pNode1;
    private PollableInterface pDot1;
    private PollableService pDot1Sleep;
    private PollableService pDot1Icmp;

    private OutageAnticipator m_outageAnticipator;
    private MockPollerConfig m_pollerConfig;

    private MockScheduler m_scheduler;
    private MockTimer m_timer;

    @Before
    public void setUp() throws Exception {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();

        MockUtil.println("------------ Begin Test --------------------------");

        MockLogAppender.setupLogging();

        m_mockNetwork = new MockNetwork();
        m_mockNetwork.addNode(1, "Router");
        m_mockNetwork.addInterface("192.168.1.1");
        m_mockNetwork.addService("ICMP");
        m_mockNetwork.addService("SLEEP");

        m_db = new MockDatabase();
        m_db.populate(m_mockNetwork);

        m_anticipator = new EventAnticipator();
        m_outageAnticipator = new OutageAnticipator(m_db);

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);

        m_pollContext = new MockPollContext();
        m_pollContext.setDatabase(m_db);
        m_pollContext.setCriticalServiceName("ICMP");
        m_pollContext.setNodeProcessingEnabled(true);
        m_pollContext.setPollingAllIfCritServiceUndefined(true);
        m_pollContext.setServiceUnresponsiveEnabled(true);
        m_pollContext.setEventMgr(m_eventMgr);
        m_pollContext.setMockNetwork(m_mockNetwork);

        m_pollerConfig = new MockPollerConfig(m_mockNetwork);
        m_pollerConfig.setNodeOutageProcessingEnabled(true);
        m_pollerConfig.setCriticalService("ICMP");
        m_pollerConfig.addPackage("TestPackage");
        m_pollerConfig.addDowntime(100L, 0L, 500L, false);
        m_pollerConfig.addDowntime(200L, 500L, 1500L, false);
        m_pollerConfig.addDowntime(500L, 1500L, -1L, true);
        m_pollerConfig.setDefaultPollInterval(DEFAULT_INTERVAL);
        m_pollerConfig.populatePackage(m_mockNetwork);

        m_timer = new MockTimer();
        m_scheduler = new MockScheduler(m_timer);
        m_network = createPollableNetwork(m_db, m_scheduler, m_pollerConfig, m_pollerConfig, m_pollContext);

        // set members to make the tests easier
        mNode1 = m_mockNetwork.getNode(1);
        mDot1 = mNode1.getInterface("192.168.1.1");
        mDot1Sleep = mDot1.getService("SLEEP");
        mDot1Icmp = mDot1.getService("ICMP");

        assignPollableMembers(m_network);

    }

    private void assignPollableMembers(PollableNetwork pNetwork) throws UnknownHostException {
        pNode1 = pNetwork.getNode(1);
        pDot1 = pNode1.getInterface(InetAddressUtils.addr("192.168.1.1"));
        pDot1Sleep = pDot1.getService("SLEEP");
        pDot1Icmp = pDot1.getService("ICMP");
    }

    static class InitCause extends PollableVisitorAdaptor {

        private final PollEvent m_cause;

        public InitCause(PollEvent cause) {
            m_cause = cause;
        }

        @Override
        public void visitElement(PollableElement element) {
            if (!element.hasOpenOutage()) {
                element.setCause(m_cause);
            }
        }
    }

    private PollableNetwork createPollableNetwork(final DataSource db, final ScheduleTimer scheduler, final PollerConfig pollerConfig, final PollOutagesConfig pollOutageConfig, PollContext pollContext) throws UnknownHostException {

        final PollableNetwork pNetwork = new PollableNetwork(pollContext);

        String sql = "select ifServices.nodeId as nodeId, node.nodeLabel as nodeLabel, ifServices.ipAddr as ipAddr, ifServices.serviceId as serviceId, service.serviceName as serviceName, outages.svcLostEventId as svcLostEventId, events.eventUei as svcLostEventUei, outages.ifLostService as ifLostService, outages.ifRegainedService as ifRegainedService "
          + "from ifServices "
          + "join node on ifServices.nodeId = node.nodeId "
          + "join service on ifServices.serviceId = service.serviceId "
          + "left outer join outages on "
          + "ifServices.nodeId = outages.nodeId and "
          + "ifServices.ipAddr = outages.ipAddr and "
          + "ifServices.serviceId = outages.serviceId and "
          + "ifRegainedService is null "
          + "left outer join events on outages.svcLostEventId = events.eventid "
          + "where ifServices.status = 'A'";

        Querier querier = new Querier(db, sql) {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                int nodeId = rs.getInt("nodeId");
                String nodeLabel = rs.getString("nodeLabel");
                String ipAddr = rs.getString("ipAddr");
                String serviceName = rs.getString("serviceName");
                Date date = rs.getTimestamp("ifLostService");
                Number svcLostEventId = (Number) rs.getObject("svcLostEventId");
                String svcLostUei = rs.getString("svcLostEventUei");

                addServiceToNetwork(pNetwork, nodeId, nodeLabel, ipAddr,
                  serviceName, svcLostEventId, svcLostUei,
                  date, scheduler, pollerConfig,
                  pollOutageConfig);

                // schedule.schedule();
                //MockUtil.println("Created Pollable Service "+svc+" with package "+pkg.getName());
            }

        };
        querier.execute();

        pNetwork.recalculateStatus();
        pNetwork.propagateInitialCause();
        pNetwork.resetStatusChanged();
        return pNetwork;
    }

    @After
    public void tearDown() throws Exception {

        m_eventMgr.finishProcessingEvents();
        //MockLogAppender.assertNoWarningsOrGreater();
        m_db.drop();
    }

    @Test
    public void testPollTimeLessThanInterval() {
        LOG.debug("<<<< starting testPollTimeLongerThanInterval >>>>\n");
        Service svc = m_pollerConfig.getServiceInPackage("SLEEP", m_pollerConfig.getPackage("TestPackage"));
        svc.addParameter("timeout", "500");

        long startTime = System.currentTimeMillis();
        pDot1Sleep.doPoll();
        long endTime = System.currentTimeMillis();
        LOG.debug("Service poll time: {} ms", (endTime - startTime));
        assertTrue("Service poll time was greater than polling interval", (endTime - startTime) <= DEFAULT_INTERVAL);
        assertTrue("isUp is true", pDot1Sleep.getStatus().isUp());
    }

    @Test
    public void testPollTimeLongerThanInterval() {
        LOG.debug("<<<< starting testPollTimeLongerThanInterval >>>>\n");
        Service svc = m_pollerConfig.getServiceInPackage("SLEEP", m_pollerConfig.getPackage("TestPackage"));
        svc.addParameter("timeout", "5000");

        anticipateDown(mDot1Sleep);
        long startTime = System.currentTimeMillis();
        pDot1Sleep.doPoll();
        long endTime = System.currentTimeMillis();
        LOG.debug("Service poll time: {} ms", (endTime - startTime));
        assertTrue("Service poll time was greater than polling interval", (endTime - startTime) > DEFAULT_INTERVAL);
        assertFalse("isUp is false", pDot1Sleep.getStatus().isUp());
    }

    protected org.opennms.netmgt.config.poller.Package findPackageForService(PollerConfig pollerConfig, String ipAddr, String serviceName) {
        Enumeration<org.opennms.netmgt.config.poller.Package> en = pollerConfig.enumeratePackage();
        org.opennms.netmgt.config.poller.Package lastPkg = null;
        while (en.hasMoreElements()) {
            org.opennms.netmgt.config.poller.Package pkg = (org.opennms.netmgt.config.poller.Package) en.nextElement();
            if (pollableServiceInPackage(pollerConfig, ipAddr, serviceName, pkg)) {
                lastPkg = pkg;
            }
        }
        return lastPkg;
    }

    private boolean pollableServiceInPackage(PollerConfig pollerConfig, String ipAddr, String serviceName, org.opennms.netmgt.config.poller.Package pkg) {
        return (pollerConfig.isServiceInPackageAndEnabled(serviceName, pkg)
          && pollerConfig.isInterfaceInPackage(ipAddr, pkg));
    }

    private InetAddress getInetAddress(String ipAddr) {
        InetAddress addr;
        addr = InetAddressUtils.addr(ipAddr);
        if (addr == null) {
            // in 'real life' I would just log this and contine with the others
            throw new RuntimeException("Error converting " + ipAddr + " to an InetAddress");
        }
        return addr;
    }

    private PollableService addServiceToNetwork(final PollableNetwork pNetwork,
      int nodeId, String nodeLabel, String ipAddr, String serviceName,
      Number svcLostEventId, String svcLostUei,
      Date svcLostTime, final ScheduleTimer scheduler,
      final PollerConfig pollerConfig,
      final PollOutagesConfig pollOutageConfig) {
        InetAddress addr = getInetAddress(ipAddr);

        org.opennms.netmgt.config.poller.Package pkg = findPackageForService(pollerConfig, ipAddr, serviceName);
        if (pkg == null) {
            MockUtil.println("No package for service " + serviceName + " with ipAddr " + ipAddr);
            return null;
        }

        PollableService svc = pNetwork.createService(nodeId, nodeLabel, addr, serviceName);
        PollableServiceConfig pollConfig = new PollableServiceConfig(svc, pollerConfig, pollOutageConfig, pkg, scheduler);
        svc.setPollConfig(pollConfig);
        if (serviceName.equals("SLEEP")) {
            ServiceMonitor sm = new SleepingServiceMonitor();
            sm.initialize(svc);
            pollConfig.setServiceMonitor(sm);
        }
        synchronized (svc) {
            if (svc.getSchedule() == null) {
                Schedule schedule = new Schedule(svc, pollConfig, scheduler);
                svc.setSchedule(schedule);
            }
        }

        if (svcLostEventId == null) {
            if (svc.getParent().getStatus().isUnknown()) {
                svc.updateStatus(PollStatus.up());
            } else {
                svc.updateStatus(svc.getParent().getStatus());
                svc.setCause(svc.getParent().getCause());
            }
        } else {
            svc.updateStatus(PollStatus.down());

            PollEvent cause = new DbPollEvent(svcLostEventId.intValue(), svcLostUei, svcLostTime);
            svc.setCause(cause);
        }
        return svc;
    }

    /**
     * @param svc
     */
    private void anticipateDown(MockElement element) {
        Event event = element.createDownEvent();
        m_anticipator.anticipateEvent(event);
        m_outageAnticipator.anticipateOutageOpened(element, event);
    }

    private class SleepingServiceMonitor implements ServiceMonitor {

        private final long DEFAULT_TIMEOUT = 5000L;

        private final Logger LOG = LoggerFactory.getLogger(SleepingServiceMonitor.class);
        private long m_sleepTime = DEFAULT_TIMEOUT;

        @Override
        public void initialize(Map<String, Object> parameters) {
            if (parameters != null) {
                if (parameters.containsKey("timeout")) {
                    m_sleepTime = Long.valueOf((String) parameters.get("timeout"));
                }
            }
        }

        @Override
        public void release() {
        }

        @Override
        public void initialize(MonitoredService svc) {
        }

        @Override
        public void release(MonitoredService svc) {
        }

        @Override
        public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
            long timeout = m_sleepTime;
            if (parameters != null) {
                if (parameters.containsKey("timeout")) {
                    timeout = Long.valueOf((String) parameters.get("timeout"));
                }
            }
            LOG.debug("polling starting for service. timeout in {} ms", timeout);
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException ex) {
                LOG.error("Interrupted", ex);
            }
            LOG.debug("polling finished for service.");
            return PollStatus.up();
        }

    }

}
