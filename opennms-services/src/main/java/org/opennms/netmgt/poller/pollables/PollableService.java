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
package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.util.Date;
import java.util.Map;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.scheduler.PostponeNecessary;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a PollableService
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PollableService extends PollableElement implements ReadyRunnable, MonitoredService {
    
    private static final Logger LOG = LoggerFactory.getLogger(PollableService.class);

    private final class PollRunner implements Runnable {
    	
    	private volatile PollStatus m_pollStatus;
            @Override
		public void run() {
		    doPoll();
		    getNode().processStatusChange(new Date());
		    m_pollStatus = getStatus();
		}
		public PollStatus getPollStatus() {
			return m_pollStatus;
		}
	}

	private final String m_svcName;

    private volatile PollConfig m_pollConfig;
    private volatile PollStatus m_oldStatus;
    private volatile Schedule m_schedule;
    private volatile long m_statusChangeTime = 0L;
    private volatile PollStatus m_preemptivePollStatus;

    /**
     * <p>Constructor for PollableService.</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param iface a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    public PollableService(PollableInterface iface, String svcName) {
        super(iface, Scope.SERVICE);
        m_svcName = svcName;
    }

    @Override
    protected boolean isSuspended() {
        return getSchedule().getInterval().scheduledSuspension();
    }

    /**
     * <p>getInterface</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    public PollableInterface getInterface() {
        return (PollableInterface)getParent();
    }
    
    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    public PollableNode getNode() {
        return getInterface().getNode();
    }

    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     */
    public PollableNetwork getNetwork() {
        return getInterface().getNetwork();
    }
    
    /**
     * <p>getContext</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollContext} object.
     */
    @Override
    public PollContext getContext() {
        return getInterface().getContext();
    }
    /**
     * <p>getSvcName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getSvcName() {
        return m_svcName;
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getIpAddr() {
        return getInterface().getIpAddr();
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    @Override
    public int getNodeId() {
        return getInterface().getNodeId();
    }

    
    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getNodeLabel() {
        return getInterface().getNodeLabel();
    }

    public String getNodeLocation() {
        return getInterface().getNodeLocation();
    }

    /** {@inheritDoc} */
    @Override
    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitService(this);
    }

    /**
     * <p>setPollConfig</p>
     *
     * @param pollConfig a {@link org.opennms.netmgt.poller.pollables.PollableServiceConfig} object.
     */
    public void setPollConfig(PollableServiceConfig pollConfig) {
        m_pollConfig = pollConfig;
    }

    public PollConfig getPollConfig() {
        return m_pollConfig;
    }

    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    @Override
    public PollStatus poll() {
        PollStatus newStatus;
        if (m_preemptivePollStatus != null) {
            newStatus = m_preemptivePollStatus;
        } else {
            newStatus = m_pollConfig.poll();
        }
        if (!newStatus.isUnknown()) { 
            updateStatus(newStatus);
        }
        return getStatus();
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
     * <p>doPoll</p>
     *
     * @return the top changed element whose status changes needs to be processed
     */
    public PollStatus doPoll() {
        if (getContext().isNodeProcessingEnabled()) {
            return getParent().doPoll(this);
        }
        else {
            resetStatusChanged();
            return poll();
        }
    }
    

    
    /** {@inheritDoc} */
    @Override
    public Event createDownEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), date, getStatus().getReason());
    }
    
    
    /** {@inheritDoc} */
    @Override
    public Event createUpEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), date, getStatus().getReason());
    }
    
    /**
     * <p>createUnresponsiveEvent</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createUnresponsiveEvent(Date date) {
        return getContext().createEvent(EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), date, getStatus().getReason());
    }

    /**
     * <p>createResponsiveEvent</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createResponsiveEvent(Date date) {
        return getContext().createEvent(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), date, getStatus().getReason());
    }

    /** {@inheritDoc} */
    @Override
    public void createOutage(PollEvent cause) {
        super.createOutage(cause);
        getContext().openOutage(this, cause);
    }
    /** {@inheritDoc} */
    @Override
    protected void resolveOutage(PollEvent resolution) {
        super.resolveOutage(resolution);
        getContext().resolveOutage(this, resolution);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return String.format("PollableService[location=%s, interface=%s, svcName=%s]",
                getNodeLocation(), getInterface(), getSvcName());
    }

    /** {@inheritDoc} */
    @Override
    public void processStatusChange(Date date) {
        
        if (getContext().isServiceUnresponsiveEnabled()) {
            if (isStatusChanged() && getStatus().equals(PollStatus.unresponsive())) {
                getContext().sendEvent(createUnresponsiveEvent(date));
                if (m_oldStatus.equals(PollStatus.up()))
                    resetStatusChanged();
            }
            else if (isStatusChanged() && m_oldStatus.equals(PollStatus.unresponsive())) {
                getContext().sendEvent(createResponsiveEvent(date));
                if (getStatus().equals(PollStatus.up()))
                    resetStatusChanged();
            }
        }
        super.processStatusChange(date);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateStatus(PollStatus newStatus) {
        
        if (!getContext().isServiceUnresponsiveEnabled()) {
            if (newStatus.equals(PollStatus.unresponsive()))
                newStatus = PollStatus.down();
        }
        
        PollStatus currentStatus = getStatus();
        if (!currentStatus.equals(newStatus)) {
            m_oldStatus = getStatus();
            setStatusChangeTime(m_pollConfig.getCurrentTime());
        }
            
        
        super.updateStatus(newStatus);
        
        if (!currentStatus.equals(newStatus)) {
            getSchedule().adjustSchedule();
        }
    }

    /**
     * <p>setSchedule</p>
     *
     * @param schedule a {@link org.opennms.netmgt.scheduler.Schedule} object.
     */
    public synchronized void setSchedule(Schedule schedule) {
        m_schedule = schedule;
    }
    
    /**
     * <p>getSchedule</p>
     *
     * @return a {@link org.opennms.netmgt.scheduler.Schedule} object.
     */
    public synchronized Schedule getSchedule() {
        return m_schedule;
    }
    
    /**
     * <p>getStatusChangeTime</p>
     *
     * @return a long.
     */
    public long getStatusChangeTime() {
        return m_statusChangeTime;
    }
    private void setStatusChangeTime(long statusChangeTime) {
        m_statusChangeTime = statusChangeTime;
    }
    
    /**
     * <p>isReady</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isReady() {
		/* FIXME: There is a bug in the Scheduler that only checks the first service in a queue.
		 * If a thread hangs the below line will cause all services with the same interval to get
		 * hang behind a service that is blocked if it has the same polling interval.  The below would
		 * be the optimal way to do it to promote fairness but... not for now.
		 */
        //return isTreeLockAvailable();
		return true;
		
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    /**
     * <p>run</p>
     */
    @Override
    public void run() {
        if(getContext().isNodeProcessingEnabled() && getContext().isAsyncEngineEnabled()) {
            getContext().getAsyncPollingEngine().triggerScheduledPollOnService(this);
        } else {
            doRun(500);
        }
    }

    /**
     * <p>doRun</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PollStatus doRun() {
    	return doRun(0);
    }

    public void doRunWithPreemptivePollStatus(PollStatus pollStatus) {
        withTreeLock(() -> {
            m_preemptivePollStatus = pollStatus;
            PollRunner r = new PollRunner();
            r.run();
            // Track the result of the poll, do this here since we short circuit PollableServiceConfig::poll
            getContext().trackPoll(this, pollStatus);
            m_preemptivePollStatus = null;
        });
    }

    private PollStatus doRun(int timeout) {
        final Map<String, String> mdc = Logging.getCopyOfContextMap();
        try {
            Logging.putThreadContext("service", m_svcName);
            Logging.putThreadContext("ipAddress", getIpAddr());
            Logging.putThreadContext("nodeId", Integer.toString(getNodeId()));
            Logging.putThreadContext("nodeLabel", getNodeLabel());
            long startDate = System.currentTimeMillis();
            LOG.debug("Start Scheduled Poll of service {}", this);
            PollStatus status;
            if (getContext().isNodeProcessingEnabled()) {
                PollRunner r = new PollRunner();
                try {
                    withTreeLock(r, timeout);
                } catch (LockUnavailable e) {
                    LOG.trace("Postponing poll for {}. Another service is currently holding the lock.", this);
                    throw new PostponeNecessary("LockUnavailable postpone poll");
                }
                status = r.getPollStatus();
            }
            else {
                doPoll();
                processStatusChange(new Date());
                status = getStatus();
            }
            LOG.debug("Finish Scheduled Poll of service {}, started at {}", this, new Date(startDate));
            return status;
        } finally {
            Logging.setContextMap(mdc);
        }
    }

	/**
     * <p>delete</p>
     */
    @Override
    public void delete() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                PollableService.super.delete();
                m_schedule.unschedule();
            }
        };
        withTreeLock(r);
    }

    /**
     * <p>schedule</p>
     */
    public void schedule() {
        if (m_schedule == null)
            throw new IllegalStateException("Cannot schedule a service whose schedule is set to null");
        
        m_schedule.schedule();
    }

    public void sendDeleteEvent(final boolean ignoreUnmanaged) {
        final Event event = getContext().createEvent(EventConstants.DELETE_SERVICE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), new Date(), getStatus().getReason());
        if (ignoreUnmanaged) {
            final Parm parm = new Parm();
            parm.setParmName(EventConstants.PARM_IGNORE_UNMANAGED);
            parm.setValue(null);
            event.addParm(new Parm(EventConstants.PARM_IGNORE_UNMANAGED, "true"));
        }
        getContext().sendEvent(event);
    }

    /**
     * <p>refreshConfig</p>
     */
    public void refreshConfig() {
        m_pollConfig.refresh();
    }

}
