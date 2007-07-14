package org.opennms.netmgt.correlation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;

public abstract class AbstractCorrelationEngine implements CorrelationEngine {

    private static int s_lastTimerId = 0;
    private EventIpcManager m_eventIpcManager;
    private Timer m_scheduler;
    private Map<Integer, TimerTask> m_pendingTasks = Collections.synchronizedMap(new HashMap<Integer, TimerTask>());

    abstract public void correlate(Event e);

    abstract public List<String> getInterestingEvents();
    
    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        m_eventIpcManager = eventIpcManager;
    }
    
    public void sendEvent(Event e) {
        m_eventIpcManager.sendNow(e);
    }
    
    public Integer setTimer( long millis ) {
        RuleTimerTask task = getTimerTask();
        m_scheduler.schedule(task, millis);
        return task.getId();
    }
    
    public RuleTimerTask getTimerTask() {
        RuleTimerTask timerTask = new RuleTimerTask();
        m_pendingTasks.put(timerTask.getId(), timerTask);
        return timerTask;
    }
    
    public void cancelTimer( Integer timerId ) {
        TimerTask task = m_pendingTasks.remove(timerId);
        if (task != null) {
            task.cancel();
        }
    }
    
    protected abstract void timerExpired(Integer timerId);
    
    public void setScheduler(Timer scheduler) {
        m_scheduler = scheduler;
    }
    
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
