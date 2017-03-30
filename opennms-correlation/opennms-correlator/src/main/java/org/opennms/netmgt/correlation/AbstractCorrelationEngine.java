/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation;

import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>Abstract AbstractCorrelationEngine class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public abstract class AbstractCorrelationEngine implements CorrelationEngine {

	private static final AtomicInteger s_lastTimerId = new AtomicInteger(0);
    private EventIpcManager m_eventIpcManager;
    private ScheduledExecutorService m_scheduler;
    private final Map<Integer, ScheduledFuture<?>> m_pendingTasks = new ConcurrentHashMap<Integer, ScheduledFuture<?>>();

    /** {@inheritDoc} */
        @Override
    public abstract void correlate(Event e);

    /**
     * <p>getInterestingEvents</p>
     *
     * @return a {@link java.util.List} object.
     */
        @Override
    public abstract List<String> getInterestingEvents();
    
    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public void setEventIpcManager(final EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }
    
    /**
     * <p>sendEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void sendEvent(final Event e) {
        m_eventIpcManager.sendNow(e);
    }
    
    /**
     * <p>setTimer</p>
     *
     * @param millis a long.
     * @return a {@link java.lang.Integer} object.
     */
    public Integer setTimer(final long millis) {
        final RuleTimerTask timerTask = new RuleTimerTask();
        ScheduledFuture<?> future = m_scheduler.schedule(timerTask, millis, TimeUnit.MILLISECONDS);
        m_pendingTasks.put(timerTask.getId(), future);
        return timerTask.getId();
    }
    
    /**
     * <p>cancelTimer</p>
     *
     * @param timerId a {@link java.lang.Integer} object.
     */
    public void cancelTimer(final Integer timerId) {
        final ScheduledFuture<?> task = m_pendingTasks.remove(timerId);
        if (task != null) {
            task.cancel(true);
        }
    }
    
    public int getPendingTasksCount() {
        return m_pendingTasks.size();
    }

    /**
     * <p>timerExpired</p>
     *
     * @param timerId a {@link java.lang.Integer} object.
     */
    protected abstract void timerExpired(Integer timerId);
    
    /**
     * <p>setScheduler</p>
     *
     * @param scheduler a {@link java.util.Timer} object.
     */
    public void setScheduler(final ScheduledExecutorService scheduler) {
        m_scheduler = scheduler;
    }
    
    /**
     * <p>runTimer</p>
     *
     * @param task a {@link org.opennms.netmgt.correlation.AbstractCorrelationEngine.RuleTimerTask} object.
     */
    protected void runTimer(final RuleTimerTask task) {
        m_pendingTasks.remove(task.getId());
        timerExpired(task.getId());
    }
    
    private class RuleTimerTask extends TimerTask {
        
        private final Integer m_id;
        
        public RuleTimerTask() {
            m_id = s_lastTimerId.incrementAndGet();
        }
        
        public Integer getId() {
            return m_id;
        }

        @Override
        public void run() {
            runTimer(this);
        }
        
    }
 
}
