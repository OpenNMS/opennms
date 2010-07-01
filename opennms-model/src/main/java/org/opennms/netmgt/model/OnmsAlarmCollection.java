package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsAlarmCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="alarms")
public class OnmsAlarmCollection extends LinkedList<OnmsAlarm> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for OnmsAlarmCollection.</p>
	 */
	public OnmsAlarmCollection() {
        super();
    }

    /**
     * <p>Constructor for OnmsAlarmCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsAlarmCollection(Collection<? extends OnmsAlarm> c) {
        super(c);
    }

    /**
     * <p>getNotifications</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="alarm")
    public List<OnmsAlarm> getNotifications() {
        return this;
    }

    /**
     * <p>setEvents</p>
     *
     * @param events a {@link java.util.List} object.
     */
    public void setEvents(List<OnmsAlarm> events) {
        clear();
        addAll(events);
    }
    
    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @XmlAttribute(name="count")
    public Integer getCount() {
    	return this.size();
    }
}

