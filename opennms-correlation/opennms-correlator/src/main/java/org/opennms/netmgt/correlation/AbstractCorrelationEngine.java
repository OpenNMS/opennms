/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created January 31, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.correlation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>Abstract AbstractCorrelationEngine class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public abstract class AbstractCorrelationEngine implements CorrelationEngine {

    private static int s_lastTimerId = 0;
    private EventIpcManager m_eventIpcManager;
    private Timer m_scheduler;
    private Map<Integer, TimerTask> m_pendingTasks = Collections.synchronizedMap(new HashMap<Integer, TimerTask>());

    /** {@inheritDoc} */
    abstract public void correlate(Event e);

    /**
     * <p>getInterestingEvents</p>
     *
     * @return a {@link java.util.List} object.
     */
    abstract public List<String> getInterestingEvents();
    
    /**
     * <p>setEventIpcManager</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }
    
    /**
     * <p>sendEvent</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void sendEvent(Event e) {
        m_eventIpcManager.sendNow(e);
    }
    
    /**
     * <p>setTimer</p>
     *
     * @param millis a long.
     * @return a {@link java.lang.Integer} object.
     */
    public Integer setTimer( long millis ) {
        RuleTimerTask task = getTimerTask();
        m_scheduler.schedule(task, millis);
        return task.getId();
    }
    
    /**
     * <p>getTimerTask</p>
     *
     * @return a {@link org.opennms.netmgt.correlation.AbstractCorrelationEngine.RuleTimerTask} object.
     */
    public RuleTimerTask getTimerTask() {
        RuleTimerTask timerTask = new RuleTimerTask();
        m_pendingTasks.put(timerTask.getId(), timerTask);
        return timerTask;
    }
    
    /**
     * <p>cancelTimer</p>
     *
     * @param timerId a {@link java.lang.Integer} object.
     */
    public void cancelTimer( Integer timerId ) {
        TimerTask task = m_pendingTasks.remove(timerId);
        if (task != null) {
            task.cancel();
        }
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
    public void setScheduler(Timer scheduler) {
        m_scheduler = scheduler;
    }
    
    /**
     * <p>runTimer</p>
     *
     * @param task a {@link org.opennms.netmgt.correlation.AbstractCorrelationEngine.RuleTimerTask} object.
     */
    public void runTimer(RuleTimerTask task) {
        m_pendingTasks.remove(task.getId());
        timerExpired(task.getId());
    }
    
    private class RuleTimerTask extends TimerTask {
        
        private Integer m_id;
        
        public RuleTimerTask() {
            m_id = ++s_lastTimerId;
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
