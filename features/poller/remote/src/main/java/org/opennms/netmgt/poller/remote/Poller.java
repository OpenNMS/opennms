/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.poller.remote.PollerFrontEnd.PollerFrontEndStates;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.util.Assert;

/**
 * <p>This {@link Poller} class listens for changes to the polling configuration
 * of a {@link PollerFrontEnd} and reacts by rescheduling the Quartz polling tasks
 * if necessary.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class Poller implements InitializingBean, ConfigurationChangedListener, PropertyChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(Poller.class);

    public final static String LOG4J_CATEGORY = "poller";

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
    @Override
    public void afterPropertiesSet() throws Exception {
        assertNotNull(m_scheduler, "scheduler");
        assertNotNull(m_pollerFrontEnd, "pollerFrontEnd");

        m_pollerFrontEnd.addConfigurationChangedListener(this);
        m_pollerFrontEnd.addPropertyChangeListener(this);

        if (m_pollerFrontEnd.isStarted()) {
            schedulePolls();
        } else {
            LOG.debug("Poller not yet registered");
        }

    }

    private void unschedulePolls() throws Exception {
        if (m_scheduler.isShutdown()) {
            // no need to unschedule in this case
            return;
        }
        for (JobKey jobKey : m_scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(PollJobDetail.GROUP))) {
            m_scheduler.deleteJob(jobKey);
        }
    }

    private void schedulePolls() throws Exception {

        LOG.debug("Enter schedulePolls");

        Collection<PolledService> polledServices = m_pollerFrontEnd.getPolledServices();

        if (polledServices == null || polledServices.size() == 0) {
            LOG.warn("No polling scheduled.");
            LOG.debug("Exit schedulePolls");
            return;
        }

        long startTime = System.currentTimeMillis();
        long scheduleSpacing = m_initialSpreadTime / polledServices.size();

        for (PolledService polledService : polledServices) {

            String jobName = polledService.toString();

            // remove any currently scheduled job
            if (m_scheduler.deleteJob(new JobKey(jobName, PollJobDetail.GROUP))) {
                LOG.debug("Job for {} already scheduled.  Rescheduling", polledService);
            } else {
                LOG.debug("Scheduling job for {}", polledService);
            }

            Date initialPollTime = new Date(startTime);

            m_pollerFrontEnd.setInitialPollTime(polledService.getServiceId(), initialPollTime);

            SimpleTriggerFactoryBean triggerFactory = new PolledServiceTrigger(polledService); 
            triggerFactory.setStartTime(initialPollTime);
            Trigger pollTrigger = triggerFactory.getObject();

            PollJobDetail jobDetail = new PollJobDetail(jobName, PollJob.class);
            jobDetail.setPolledService(polledService);
            jobDetail.setPollerFrontEnd(m_pollerFrontEnd);


            m_scheduler.scheduleJob(jobDetail, pollTrigger);

            startTime += scheduleSpacing;
        }

        LOG.debug("Exit schedulePolls");

    }

    private static void assertNotNull(Object propertyValue, String propertyName) {
        Assert.state(propertyValue != null, propertyName+" must be set for instances of "+Poller.class);
    }

    /** {@inheritDoc} */
    @Override
    public void configurationChanged(PropertyChangeEvent e) {
        try {
            unschedulePolls();
            schedulePolls();
        } catch (Throwable ex) {
            LOG.error("Unable to schedule polls!", ex);
            throw new RuntimeException("Unable to schedule polls!");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            if (Boolean.TRUE.equals(evt.getNewValue())) {
                // If the poller is paused, disconnected, or exiting then unschedule all of the
                // polling tasks so that the scheduler can shut down cleanly
                if (
                    PollerFrontEndStates.paused.toString().equals(evt.getPropertyName()) ||
                    PollerFrontEndStates.disconnected.toString().equals(evt.getPropertyName()) ||
                    PollerFrontEndStates.exitNecessary.toString().equals(evt.getPropertyName())
                ) {
                    unschedulePolls();
                }
            } else {
                if (PollerFrontEndStates.paused.toString().equals(evt.getPropertyName()) ) {
                    schedulePolls();
                } else if (PollerFrontEndStates.disconnected.toString().equals(evt.getPropertyName())) {
                    schedulePolls();
                } else if (PollerFrontEndStates.started.toString().equals(evt.getPropertyName())) {
                    unschedulePolls();
                }
            }
        } catch (Throwable ex) {
            LOG.error("Unable to schedule polls!", ex);
            throw new RuntimeException("Unable to schedule polls!");
        }
    }
}
