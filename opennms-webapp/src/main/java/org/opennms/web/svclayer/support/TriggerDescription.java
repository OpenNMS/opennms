package org.opennms.web.svclayer.support;

import java.util.Date;

/**
 * <p>TriggerDescription class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class TriggerDescription {
    
    private String m_triggerName;
    private String m_description;
    private Date m_nextFireTime;
    
    /**
     * <p>getTriggerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTriggerName() {
        return m_triggerName;
    }
    
    /**
     * <p>setTriggerName</p>
     *
     * @param triggerName a {@link java.lang.String} object.
     */
    public void setTriggerName(String triggerName) {
        m_triggerName = triggerName;
    }
    
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }
    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }

    /**
     * <p>getNextFireTime</p>
     *
     * @return a java$util$Date object.
     */
    public Date getNextFireTime() {
        return m_nextFireTime;
    }

    /**
     * <p>setNextFireTime</p>
     *
     * @param nextFireTime a java$util$Date object.
     */
    public void setNextFireTime(Date nextFireTime) {
        m_nextFireTime = nextFireTime;
    }
    
    

}
