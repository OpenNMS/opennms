package org.opennms.web.controller.support;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.rt.RTTicket;

public class SupportResults implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean m_success = true;
    private boolean m_needsLogin = false;
    private String m_baseUrl;
    private String m_username;
    private String m_queue;
    private List<RTTicket> m_latestTickets;
    private String m_message;

    public SupportResults() {
    }

    public boolean getSuccess() {
        return m_success;
    }
    
    public void setSuccess(final boolean success) {
        m_success = success;
    }

    public boolean getNeedsLogin() {
        return m_needsLogin;
    }
    
    public void setNeedsLogin(final boolean needsLogin) {
        m_needsLogin = needsLogin;
    }
    
    public String getRTUrl() {
        return m_baseUrl;
    }

    public void setRTUrl(final String baseUrl) {
        m_baseUrl = baseUrl;
    }

    public String getUsername() {
        return m_username;
    }
    
    public void setUsername(final String username) {
        m_username = username;
    }
    
    public String getQueue() {
        return m_queue;
    }
    
    public void setQueue(final String queue) {
        m_queue = queue;
    }

    public String getMessage() {
        return m_message;
    }

    public void setMessage(final String message) {
        m_message = message;
    }

    public List<RTTicket> getLatestTickets() {
        return m_latestTickets;
    }

    public void setLatestTickets(final List<RTTicket> tickets) {
        m_latestTickets = tickets;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("success", m_success)
            .append("needsLogin", m_needsLogin)
            .append("baseUrl", m_baseUrl)
            .append("username", m_username)
            .append("queue", m_queue)
            .append("message", m_message)
            .append("latestTickets", m_latestTickets)
            .toString();
    }

}
