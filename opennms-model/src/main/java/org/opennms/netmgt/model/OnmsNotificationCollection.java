package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="notifications")
public class OnmsNotificationCollection extends LinkedList<OnmsNotification> {

	private static final long serialVersionUID = 1L;

	public OnmsNotificationCollection() {
        super();
    }

    public OnmsNotificationCollection(Collection<? extends OnmsNotification> c) {
        super(c);
    }

    @XmlElement(name="notification")
    public List<OnmsNotification> getNotifications() {
        return this;
    }

    public void setEvents(List<OnmsNotification> events) {
        clear();
        addAll(events);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

