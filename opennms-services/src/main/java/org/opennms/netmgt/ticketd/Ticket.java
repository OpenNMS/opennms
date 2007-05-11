package org.opennms.netmgt.ticketd;

import java.util.HashMap;
import java.util.Map;

public class Ticket {
    
    public enum State {
        OPEN,
        CANCELLED,
        CLOSED
    }
    
    private String m_id;
    private State m_state = State.OPEN;
    private String m_summary;
    private String m_details;
    private String m_user;
    
    
    
    private Map<String, String> m_attributes;
    
    public Map<String, String> getAttributes() {
        return m_attributes;
    }
    public void setAttributes(Map<String, String> attributes) {
        m_attributes = attributes;
    }
    
    public void addAttribute(String key, String value) {
        if (m_attributes == null) {
            m_attributes = new HashMap<String, String>();
        }
        m_attributes.put(key, value);
    }
    
    public String getAttribute(String key) {
        if (m_attributes == null) {
            return null;
        }
        return m_attributes.get(key);
    }
    
    public String getSummary() {
        return m_summary;
    }
    public void setSummary(String summary) {
        m_summary = summary;
    }
    
    public String getDetails() {
        return m_details;
    }
    public void setDetails(String details) {
        m_details = details;
    }
    
    public String getId() {
        return m_id;
    }
    public void setId(String id) {
        m_id = id;
    }
    
    public String getUser() {
        return m_user;
    }
    public void setUser(String user) {
        m_user = user;
    }
    public State getState() {
        return m_state;
    }
    public void setState(State state) {
        m_state = state;
    }
    
    
}
