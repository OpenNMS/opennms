package org.opennms.web.svclayer.support;

import java.util.Date;

public class TriggerDescription {
    
    private String m_triggerName;
    private String m_description;
    private Date m_nextFireTime;
    
    public String getTriggerName() {
        return m_triggerName;
    }
    
    public void setTriggerName(String triggerName) {
        m_triggerName = triggerName;
    }
    
    public String getDescription() {
        return m_description;
    }
    public void setDescription(String description) {
        m_description = description;
    }

    public Date getNextFireTime() {
        return m_nextFireTime;
    }

    public void setNextFireTime(Date nextFireTime) {
        m_nextFireTime = nextFireTime;
    }
    
    

}
