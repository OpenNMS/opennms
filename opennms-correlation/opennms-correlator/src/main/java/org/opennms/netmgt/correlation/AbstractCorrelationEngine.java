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

    public abstract void reloadConfig(boolean persistState);

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

    public ScheduledExecutorService getScheduler() {
        return m_scheduler;
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
