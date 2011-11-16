package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="group")
@XmlAccessorType(XmlAccessType.FIELD)
public class OnmsGroup implements Serializable {
    private static final long serialVersionUID = 3896708424386708948L;

    @XmlElement(name="name", required=true)
    private String m_name;
    
    @XmlElement(name="comments", required=false)
    private String m_comments;
    
    @XmlElement(name="user", required=false)
    private List<String> m_users = new ArrayList<String>();

    public OnmsGroup() { }
    
    public OnmsGroup(final String groupName) {
        m_name = groupName;
    }
    
    public String getName() {
        return m_name;
    }
    
    public void setName(final String name) {
        m_name = name;
    }

    public String getComments() {
        return m_comments;
    }
    
    public void setComments(final String comments) {
        m_comments = comments;
    }

    public List<String> getUsers() {
        return m_users;
    }

    public void setUsers(final List<String> users) {
        m_users = users;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("name", m_name)
            .append("comments", m_comments)
            .append("users", m_users)
            .toString();
    }
}
