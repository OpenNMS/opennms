package org.opennms.netmgt.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.opennms.netmgt.xml.event.Event;

//Soon to be model class
public class Acknowledgment {

    private final Date m_ackTime;
    private final String m_ackUser;
    
    public Acknowledgment() {
        this(new Date(), "admin");
    }
    
    public Acknowledgment(String user) {
        this(new Date(), user);
    }
    
    public Acknowledgment(final Date time) {
        this(time, "admin");
    }
    
    public Acknowledgment(Date time, String user) {
        m_ackTime = (time == null) ? new Date() : time;
        m_ackUser = (user == null) ? "admin" : user;
    }
    
    public Acknowledgment(final Event e) throws ParseException {
        this(e, "admin");
    }

    public Acknowledgment(final Event e, final String user) throws ParseException {
        m_ackTime = DateFormat.getDateInstance().parse(e.getTime());
        m_ackUser = (user == null) ? "admin" : user;
    }

    public Date getAckTime() {
        return m_ackTime;
    }

    public String getAckUser() {
        return m_ackUser;
    }


}
