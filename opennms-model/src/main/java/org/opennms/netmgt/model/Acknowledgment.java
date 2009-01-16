package org.opennms.netmgt.model;

import java.util.Date;

import org.opennms.netmgt.xml.event.Event;

//Soon to be model class
public class Acknowledgment {

    private Event m_event;
    private Date m_ackTime;
    private String m_ackUser;
    
    public Acknowledgment() {
        
    }

    public Acknowledgment(Event e) {
        m_event = e;
    }

    public Event getEvent() {
        return m_event;
    }

    public void setEvent(Event event) {
        m_event = event;
    }

    public void setAckTime(Date ackTime) {
        m_ackTime = ackTime;
    }

    public Date getAckTime() {
        return m_ackTime;
    }

    public void setAckUser(String ackUser) {
        m_ackUser = ackUser;
    }

    public String getAckUser() {
        return m_ackUser;
    }


}
