package org.opennms.netmgt.rt;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

public class RTUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private long m_id;
    private String m_username;
    private String m_realname;
    private String m_email;

    public RTUser(final long id, String username, String realname, String email) {
        m_id = id;
        m_username = username;
        m_realname = realname;
        m_email = email;
    }

    public long getId() {
        return m_id;
    }
    
    public String getUsername() {
        return m_username;
    }
    
    public String getRealname() {
        return m_realname;
    }
    
    public String getEmail() {
        return m_email;
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", m_id)
            .append("username", m_username)
            .append("realname", m_realname)
            .append("email", m_email)
            .toString();
    }
}
