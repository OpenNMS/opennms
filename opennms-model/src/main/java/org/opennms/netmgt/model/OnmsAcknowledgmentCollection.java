package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="acknowledgments")
public class OnmsAcknowledgmentCollection extends LinkedList<OnmsAcknowledgment> {

	private static final long serialVersionUID = 1L;

	public OnmsAcknowledgmentCollection() {
        super();
    }

    public OnmsAcknowledgmentCollection(Collection<? extends OnmsAcknowledgment> c) {
        super(c);
    }

    @XmlElement(name="onmsAcknowledgment")
    public List<OnmsAcknowledgment> getNotifications() {
        return this;
    }

    public void setEvents(List<OnmsAcknowledgment> events) {
        clear();
        addAll(events);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

