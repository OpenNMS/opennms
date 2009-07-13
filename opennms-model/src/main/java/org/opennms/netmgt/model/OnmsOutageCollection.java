package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="outages")
public class OnmsOutageCollection extends LinkedList<OnmsOutage> {

	private static final long serialVersionUID = 1L;

	public OnmsOutageCollection() {
        super();
    }

    public OnmsOutageCollection(Collection<? extends OnmsOutage> c) {
        super(c);
    }

    @XmlElement(name="outage")
    public List<OnmsOutage> getNotifications() {
        return this;
    }

    public void setEvents(List<OnmsOutage> events) {
        clear();
        addAll(events);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

