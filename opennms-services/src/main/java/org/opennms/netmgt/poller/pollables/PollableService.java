/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 7, 2005
 *
 * Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
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

import java.net.InetAddress;
import java.util.Date;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.scheduler.PostponeNecessary;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollableService
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PollableService extends PollableElement implements ReadyRunnable, MonitoredService {

    private final class PollRunner implements Runnable {
    	
    	private volatile PollStatus m_pollStatus;
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
    private final IPv4NetworkInterface m_netInterface;

    private volatile PollConfig m_pollConfig;
    private volatile PollStatus m_oldStatus;
    private volatile Schedule m_schedule;
    private volatile long m_statusChangeTime = 0L;
    /**
     * <p>Constructor for PollableService.</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param iface a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    public PollableService(PollableInterface iface, String svcName) {
        super(iface, Scope.SERVICE);
        m_svcName = svcName;
        m_netInterface = new IPv4NetworkInterface(iface.getAddress());
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
    public PollContext getContext() {
        return getInterface().getContext();
    }
    /**
     * <p>getSvcName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSvcName() {
        return m_svcName;
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return getInterface().getIpAddr();
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return getInterface().getNodeId();
    }
    
    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return getInterface().getNodeLabel();
    }


    /** {@inheritDoc} */
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

    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public PollStatus poll() {
        PollStatus newStatus = m_pollConfig.poll();
        if (!newStatus.isUnknown()) { 
            updateStatus(newStatus);
        }
        return getStatus();
    }

    /**
     * <p>getNetInterface</p>
     *
     * @throws UnknownHostException if any.
     * @return a {@link org.opennms.netmgt.poller.NetworkInterface} object.
     */
    public NetworkInterface getNetInterface() {
        return m_netInterface;
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
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
    public Event createDownEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), date, getStatus().getReason());
    }
    
    
    /** {@inheritDoc} */
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
    public void createOutage(PollEvent cause) {
        super.createOutage(cause);
        getContext().openOutage(this, cause);
    }
    /** {@inheritDoc} */
    protected void resolveOutage(PollEvent resolution) {
        super.resolveOutage(resolution);
        getContext().resolveOutage(this, resolution);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() { return getInterface()+":"+getSvcName(); }

    /** {@inheritDoc} */
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
    public void run() {
        doRun(500);
    }
    
    /**
     * <p>doRun</p>
     *
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public PollStatus doRun() {
    	return doRun(0);
    }

	private PollStatus doRun(int timeout) {
		long startDate = System.currentTimeMillis();
        log().debug("Start Scheduled Poll of service "+this);
        PollStatus status;
        if (getContext().isNodeProcessingEnabled()) {
            PollRunner r = new PollRunner();
            try {
				withTreeLock(r, timeout);
            } catch (LockUnavailable e) {
                log().info("Postponing poll for "+this+" because "+e);
                throw new PostponeNecessary("LockUnavailable postpone poll");
            }
            status = r.getPollStatus();
        }
        else {
            doPoll();
            processStatusChange(new Date());
            status = getStatus();
        }
        if (log().isDebugEnabled())
            log().debug("Finish Scheduled Poll of service "+this+", started at "+new Date(startDate));
        return status;
	}

	private Category log() {
		return ThreadCategory.getInstance(PollableService.class);
	}

    /**
     * <p>delete</p>
     */
    public void delete() {
        Runnable r = new Runnable() {
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

    /**
     * <p>sendDeleteEvent</p>
     */
    public void sendDeleteEvent() {
        getContext().sendEvent(getContext().createEvent(EventConstants.DELETE_SERVICE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), new Date(), getStatus().getReason()));
    }

    /**
     * <p>refreshConfig</p>
     */
    public void refreshConfig() {
        m_pollConfig.refresh();
    }

}
