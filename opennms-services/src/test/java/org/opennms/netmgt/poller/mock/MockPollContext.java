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
package org.opennms.netmgt.poller.mock;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.AsyncPollingEngine;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.pollables.DbPollEvent;
import org.opennms.netmgt.poller.pollables.PollContext;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockUtil;


public class MockPollContext implements PollContext {
    private String m_critSvcName;
    private boolean m_nodeProcessingEnabled;
    private boolean m_pollingAllIfCritServiceUndefined;
    private boolean m_serviceUnresponsiveEnabled;
    private EventIpcManager m_eventMgr;
    private MockDatabase m_db;
    private MockNetwork m_mockNetwork;
    private final AtomicLong m_idCounter = new AtomicLong(1);


    @Override
    public String getCriticalServiceName() {
        return m_critSvcName;
    }

    public void setCriticalServiceName(String svcName) {
        m_critSvcName = svcName;
    }

    @Override
    public boolean isNodeProcessingEnabled() {
        return m_nodeProcessingEnabled;
    }
    public void setNodeProcessingEnabled(boolean nodeProcessingEnabled) {
        m_nodeProcessingEnabled = nodeProcessingEnabled;
    }
    @Override
    public boolean isPollingAllIfCritServiceUndefined() {
        return m_pollingAllIfCritServiceUndefined;
    }
    public void setPollingAllIfCritServiceUndefined(boolean pollingAllIfCritServiceUndefined) {
        m_pollingAllIfCritServiceUndefined = pollingAllIfCritServiceUndefined;
    }

    public void setEventMgr(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    public void setDatabase(MockDatabase db) {
        m_db = db;
    }

    public void setMockNetwork(MockNetwork network) {
        m_mockNetwork = network;
    }

    @Override
    public PollEvent sendEvent(Event event) {
        long id = m_idCounter.getAndIncrement();
        event.setDbid(id);
        m_eventMgr.sendNow(event);
        return new DbPollEvent(id, event.getUei(), event.getTime());
    }

    @Override
    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date, String reason) {
        EventBuilder e = MockEventUtil.createEventBuilder("Test", uei, nodeId, (address == null ? null : InetAddressUtils.str(address)), svcName, reason);
        e.setTime(date);
        return e.getEvent();
    }

    @Override
    public void openOutage(final PollableService pSvc, final PollEvent svcLostEvent) {
        MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
        Timestamp eventTime = new Timestamp(svcLostEvent.getDate().getTime());
        MockUtil.println("Opening Outage for " + mSvc);
        m_db.createOutage(mSvc, svcLostEvent.getEventId(), eventTime);
    }

    @Override
    public void resolveOutage(final PollableService pSvc, final PollEvent svcRegainEvent) {
        MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
        Timestamp eventTime = new Timestamp(svcRegainEvent.getDate().getTime());
        MockUtil.println("Resolving Outage for " + mSvc);
        m_db.resolveOutage(mSvc, svcRegainEvent.getEventId(), eventTime);
    }

    @Override
    public boolean isServiceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }

    @Override
    public void trackPoll(PollableService service, PollStatus result) {
        // pass, nothing to track
    }

    @Override
    public boolean isAsyncEngineEnabled() {
        return false;
    }

    @Override
    public AsyncPollingEngine getAsyncPollingEngine() {
        return null;
    }


    public void setServiceUnresponsiveEnabled(boolean serviceUnresponsiveEnabled) {
        m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }

    public String getName() {
        return "MockPollContext";
    }
}
