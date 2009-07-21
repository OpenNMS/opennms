package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="events")
public class OnmsEventCollection extends LinkedList<OnmsEvent> {

	private static final long serialVersionUID = 1L;
	private int m_totalCount;

	public OnmsEventCollection() {
        super();
    }

    public OnmsEventCollection(Collection<? extends OnmsEvent> c) {
        super(c);
    }

    @XmlElement(name="event")
    public List<OnmsEvent> getEvents() {
        return this;
    }

    public void setEvents(List<OnmsEvent> events) {
        clear();
        addAll(events);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
    
    @XmlAttribute(name="totalCount")
    public int getTotalCount() {
        return m_totalCount;
    }
    
    public void setTotalCount(int count) {
        m_totalCount = count;
    }
}

