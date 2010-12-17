package org.opennms.netmgt.rt;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

public class RTQueue implements Serializable {
    private static final long serialVersionUID = 1L;

    private long m_id;
    private String m_name;

    public RTQueue() {
    }

    public RTQueue(final long id, final String name) {
        m_id = id;
        m_name = name;
    }
    
    public long getId() {
        return m_id;
    }
    
    public void setId(final long id) {
        m_id = id;
    }
    
    public String getName() {
        return m_name;
    }
    
    public void setName(final String name) {
        m_name = name;
    }
    
    public boolean isAccessible() {
        return true;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("id", m_id)
            .append("name", m_name)
            .append("accessible", isAccessible())
            .toString();
    }
}
