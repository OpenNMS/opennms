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
 * Created: August 16, 2006
 *
 * Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.poller.remote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.PollStatus;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>Poller class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class Poller implements InitializingBean, PollObserver, ConfigurationChangedListener, PropertyChangeListener {
	
	private PollerFrontEnd m_pollerFrontEnd;
	private Scheduler m_scheduler;
	private long m_initialSpreadTime = 300000L;
	
	/**
	 * <p>setPollerFrontEnd</p>
	 *
	 * @param pollerFrontEnd a {@link org.opennms.netmgt.poller.remote.PollerFrontEnd} object.
	 */
	public void setPollerFrontEnd(PollerFrontEnd pollerFrontEnd) {
		m_pollerFrontEnd = pollerFrontEnd;
	}

	/**
	 * <p>setScheduler</p>
	 *
	 * @param scheduler a {@link org.quartz.Scheduler} object.
	 */
	public void setScheduler(Scheduler scheduler) {
		m_scheduler = scheduler;
	}
	
	/**
	 * <p>setInitialSpreadTime</p>
	 *
	 * @param initialSpreadTime a long.
	 */
	public void setInitialSpreadTime(long initialSpreadTime) {
		m_initialSpreadTime = initialSpreadTime;
	}
	

	/**
	 * <p>afterPropertiesSet</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void afterPropertiesSet() throws Exception {
		assertNotNull(m_scheduler, "scheduler");
		assertNotNull(m_pollerFrontEnd, "pollerFrontEnd");
        
        m_pollerFrontEnd.addConfigurationChangedListener(this);
        m_pollerFrontEnd.addPropertyChangeListener(this);
		
        if (m_pollerFrontEnd.isStarted()) {
            schedulePolls();
        } else {
            log().debug("Poller not yet registered");
        }

	}
    
    private void unschedulePolls() throws Exception {
        if (m_scheduler.isShutdown()) {
            // no need to unschedule in this case
            return;
        }
        for (String jobName : m_scheduler.getJobNames(PollJobDetail.GROUP)) {
            m_scheduler.deleteJob(jobName, PollJobDetail.GROUP);
        }
    }
	
	private void schedulePolls() throws Exception {
        
        log().debug("Enter schedulePolls");
		
		Collection<PolledService> polledServices = m_pollerFrontEnd.getPolledServices();

		if (polledServices == null || polledServices.size() == 0) {
			log().warn("No polling scheduled.");
            log().debug("Exit schedulePolls");
			return;
		}

		long startTime = System.currentTimeMillis();
		long scheduleSpacing = m_initialSpreadTime / polledServices.size();
		
        for (PolledService polledService : polledServices) {
            
            String jobName = polledService.toString();

            // remove any currently scheduled job
            if (m_scheduler.deleteJob(jobName, PollJobDetail.GROUP)) {
                log().debug(String.format("Job for %s already scheduled.  Rescheduling", polledService));
            } else {
                log().debug("Scheduling job for "+polledService);
            }
			
			Date initialPollTime = new Date(startTime);
			
			m_pollerFrontEnd.setInitialPollTime(polledService.getServiceId(), initialPollTime);
			
			Trigger pollTrigger = new PolledServiceTrigger(polledService);
			pollTrigger.setStartTime(initialPollTime);
			
            PollJobDetail jobDetail = new PollJobDetail(jobName, PollJob.class);
			jobDetail.setPolledService(polledService);
			jobDetail.setPollerFrontEnd(m_pollerFrontEnd);
			
            
			m_scheduler.scheduleJob(jobDetail, pollTrigger);
			
			startTime += scheduleSpacing;
		}
		
        log().debug("Exit schedulePolls");
		
	}

	private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}

	private void assertNotNull(Object propertyValue, String propertyName) {
		Assert.state(propertyValue != null, propertyName+" must be set for instances of "+Poller.class);
	}

	/** {@inheritDoc} */
	public void pollCompleted(String pollId, PollStatus pollStatus) {
		log().info("Complete Poll for "+pollId+" status = "+pollStatus);
	}

	/** {@inheritDoc} */
	public void pollStarted(String pollId) {
		log().info("Begin Poll for "+pollId);
		
	}

    /** {@inheritDoc} */
    public void configurationChanged(PropertyChangeEvent e) {
        try {
            unschedulePolls();
            schedulePolls();
        } catch (Exception ex) {
            log().fatal("Unable to schedule polls!", ex);
            throw new RuntimeException("Unable to schedule polls!");
        }
    }

    /** {@inheritDoc} */
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (Boolean.TRUE.equals(evt.getNewValue())) {
                if ("paused".equals(evt.getPropertyName())) {
                    unschedulePolls();
                } else if ("disconnected".equals(evt.getPropertyName())) {
                    unschedulePolls();
                }
            } else {
                if ("paused".equals(evt.getPropertyName()) ) {
                    schedulePolls();
                } else if ("disconnected".equals(evt.getPropertyName())) {
                    schedulePolls();
                } else if ("started".equals(evt.getPropertyName())) {
                    unschedulePolls();
                }
            }
        } catch (Exception ex) {
            log().fatal("Unable to schedule polls!", ex);
            throw new RuntimeException("Unable to schedule polls!");
        }
    }


}
