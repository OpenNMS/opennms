package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="alarms")
public class OnmsAlarmCollection extends LinkedList<OnmsAlarm> {

	private static final long serialVersionUID = 1L;

	public OnmsAlarmCollection() {
        super();
    }

    public OnmsAlarmCollection(Collection<? extends OnmsAlarm> c) {
        super(c);
    }

    @XmlElement(name="alarm")
    public List<OnmsAlarm> getNotifications() {
        return this;
    }

    public void setEvents(List<OnmsAlarm> events) {
        clear();
        addAll(events);
    }
    
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

