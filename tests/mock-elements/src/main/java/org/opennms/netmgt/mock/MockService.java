/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>MockService class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class MockService extends MockElement implements MonitoredService {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(1);

    public static enum SvcMgmtStatus {
        ACTIVE("A"),
        DELETED("D"),
        UNMANAGED("U"),
        FORCE_UNMANAGED("F"),
        NOT_POLLED("N"),
        REMOTE_ONLY("X");
        
        private final String m_dbString;
        
        SvcMgmtStatus(String dbString) {
            m_dbString = dbString;
        }
        
        public String toDbString() {
            return m_dbString;
        }
        
        public static SvcMgmtStatus fromDbString(String dbString) {
            if (dbString == null) {
                return null;
            }
            for (SvcMgmtStatus status : SvcMgmtStatus.values()) {
                if (status.toDbString().equals(dbString)) {
                    return status;
                }
            }
            throw new IllegalArgumentException(dbString + "is not an legal SvcMgmtStatus string");
        }
     }

    private int m_pollCount;
    
    private PollStatus m_pollStatus;

    private final int m_id;

    private int m_serviceId;

    private final String m_svcName;
    
    private SvcMgmtStatus m_mgmtStatus = SvcMgmtStatus.ACTIVE;

    private List<PollAnticipator> m_triggers = new ArrayList<PollAnticipator>();

    private NetworkInterface<InetAddress> m_netAddr;

   /**
    * <p>Constructor for MockService.</p>
    *
    * @param iface a {@link org.opennms.netmgt.mock.MockInterface} object.
    * @param svcName a {@link java.lang.String} object.
    * @param serviceId a int.
    */
   public MockService(MockInterface iface, String svcName, int serviceId) {
        super(iface);
        m_id = ID_COUNTER.getAndIncrement();
        m_svcName = svcName;
        m_serviceId = serviceId;
        m_pollStatus = PollStatus.up();
        m_pollCount = 0;

    }
   
   // test
   /**
    * <p>bringDown</p>
    *
    * @param reason a {@link java.lang.String} object.
    */
   public void bringDown(String reason) {
       setServicePollStatus(PollStatus.down(reason));
   }


   // FIXME: model? make generic poll listener
    /** {@inheritDoc} */
    @Override
    public void addAnticipator(PollAnticipator trigger) {
        m_triggers.add(trigger);
    }

    // model
    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getId() {
        return m_id;
    }

    /**
     * <p>getId</p>
     *
     * @return a int.
     */
    public int getSvcId() {
        return m_serviceId;
    }

    public void setSvcId(Integer nextServiceId) {
        m_serviceId = nextServiceId;
    }

    // model
    /**
     * <p>getInterface</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockInterface} object.
     */
    public MockInterface getInterface() {
        return (MockInterface) getParent();
    }

    // model
    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getIpAddr() {
        return getInterface().getIpAddr();
    }

    // impl
    @Override
    Object getKey() {
        return m_svcName;
    }

    // model
    /**
     * <p>getSvcName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getSvcName() {
        return m_svcName;
    }

    // model
    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockNetwork} object.
     */
    @Override
    public MockNetwork getNetwork() {
        return getInterface().getNetwork();
    }

    // model
    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockNode} object.
     */
    public MockNode getNode() {
        return getInterface().getNode();
    }

    // model
    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    @Override
    public int getNodeId() {
        return getNode().getNodeId();
    }

    // model
    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getNodeLabel() {
        return getNode().getLabel();
    }

    // stats
    /**
     * <p>getPollCount</p>
     *
     * @return a int.
     */
    @Override
    public int getPollCount() {
        return m_pollCount;
    }
    
    // test
    /**
     * <p>getPollStatus</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    @Override
    public PollStatus getPollStatus() {
        return m_pollStatus;
    }
    
    /**
     * <p>getMgmtStatus</p>
     *
     * @return a {@link org.opennms.netmgt.mock.MockService.SvcMgmtStatus} object.
     */
    public SvcMgmtStatus getMgmtStatus() {
        return m_mgmtStatus;
    }
    
    /**
     * <p>setMgmtStatus</p>
     *
     * @param mgmtStatus a {@link org.opennms.netmgt.mock.MockService.SvcMgmtStatus} object.
     */
    public void setMgmtStatus(SvcMgmtStatus mgmtStatus) {
        m_mgmtStatus = mgmtStatus;
    }

    // test
    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PollStatus poll() {
        m_pollCount++;
        
        for (PollAnticipator trigger : m_triggers) {
            trigger.poll(this);
        }

        return getPollStatus();

    }

    // FIXME: model? make generic poll listener
    /** {@inheritDoc} */
    @Override
    public void removeAnticipator(PollAnticipator trigger) {
        m_triggers.remove(trigger);
    }

    // stats
    /**
     * <p>resetPollCount</p>
     */
    @Override
    public void resetPollCount() {
        m_pollCount = 0;
    }

    //  test
    /**
     * <p>setPollStatus</p>
     *
     * @param status a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public void setPollStatus(PollStatus status) {
        m_pollStatus = status;
    }

    // impl
    /** {@inheritDoc} */
    @Override
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitService(this);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
    	return new ToStringBuilder(this)
    		.append("nodeLabel", getNodeLabel())
    		.append("ipAddr", getIpAddr())
    		.append("service", getSvcName())
    		.toString();
    }

    /**
     * <p>createDownEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Override
    public Event createDownEvent() {
        return MockEventUtil.createNodeLostServiceEvent("Test", this, "Service Not Responding.");
    }

    /**
     * <p>createUpEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Override
    public Event createUpEvent() {
        return MockEventUtil.createNodeRegainedServiceEvent("Test", this);
    }

    /**
     * <p>createUnresponsiveEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createUnresponsiveEvent() {
        return MockEventUtil.createServiceUnresponsiveEvent("Test", this, String.valueOf(PollStatus.SERVICE_UNAVAILABLE));
    }

    /**
     * <p>createResponsiveEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createResponsiveEvent() {
        return MockEventUtil.createServiceResponsiveEvent("Test", this);
    }
    
    /**
     * <p>createNewEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Override
    public Event createNewEvent() {
        return MockEventUtil.createNodeGainedServiceEvent("Test", this);
    }

    /**
     * <p>createDeleteEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @Override
    public Event createDeleteEvent() {
        return MockEventUtil.createServiceDeletedEvent("Test", this);
    }

    /**
     * <p>getNetInterface</p>
     *
     * @return a {@link org.opennms.netmgt.poller.NetworkInterface} object.
     */
    @Override
    public NetworkInterface<InetAddress> getNetInterface() {
        if (m_netAddr == null)
            m_netAddr = new InetNetworkInterface(getAddress());
        
        return m_netAddr;
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    @Override
    public InetAddress getAddress() {
        return getInterface().getAddress();    
    }

	/**
	 * <p>createDemandPollEvent</p>
	 *
	 * @param demandPollId a int.
	 * @return a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public Event createDemandPollEvent(int demandPollId) {
		return MockEventUtil.createDemandPollServiceEvent("Test", this, demandPollId);
	}

    @Override
	public String getSvcUrl() {
		return null;
	}

    public Event createOutageCreatedEvent() {
        return MockEventUtil.createOutageCreatedEvent("Test", this, null);
    }

    public Event createOutageResolvedEvent() {
        return MockEventUtil.createOutageResolvedEvent("Test", this, null);
    }

}
