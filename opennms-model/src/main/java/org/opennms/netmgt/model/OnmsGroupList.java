package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "groups")
public class OnmsGroupList extends LinkedList<OnmsGroup> {
    private static final long serialVersionUID = -3120131643998397193L;
    private int m_totalCount;

    public OnmsGroupList() {
        super();
    }
    
    public OnmsGroupList(final Collection<? extends OnmsGroup> c) {
        super(c);
    }

    @XmlElement(name = "group")
    public List<OnmsGroup> getGroups() {
        return this;
    }
    
    public void setGroups(final List<OnmsGroup> groups) {
        clear();
        addAll(groups);
    }
    
    @XmlAttribute(name="count")
    public int getCount() {
        return this.size();
    }

    // The property has a getter "" but no setter. For unmarshalling, please define setters.
    public void setCount(final int count) {
    }

    @XmlAttribute(name="totalCount")
    public int getTotalCount() {
        return m_totalCount;
    }
    
    /**
     * <p>setTotalCount</p>
     *
     * @param count a int.
     */
    public void setTotalCount(final int count) {
        m_totalCount = count;
    }
}
