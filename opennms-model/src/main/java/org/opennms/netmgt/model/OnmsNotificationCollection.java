package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsNotificationCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="notifications")
public class OnmsNotificationCollection extends LinkedList<OnmsNotification> {

	/**
     * 
     */
    private static final long serialVersionUID = 1140502309473962746L;
    private int m_totalCount;

    /**
	 * <p>Constructor for OnmsNotificationCollection.</p>
	 */
	public OnmsNotificationCollection() {
        super();
    }

    /**
     * <p>Constructor for OnmsNotificationCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsNotificationCollection(Collection<? extends OnmsNotification> c) {
        super(c);
    }

    /**
     * <p>getNotifications</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="notification")
    public List<OnmsNotification> getNotifications() {
        return this;
    }

    /**
     * <p>setEvents</p>
     *
     * @param events a {@link java.util.List} object.
     */
    public void setEvents(List<OnmsNotification> events) {
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

    /**
     * <p>getTotalCount</p>
     *
     * @return a int.
     */
    @XmlAttribute(name="totalCount")
    public int getTotalCount() {
        return m_totalCount;
    }

    /**
     * <p>setTotalCount</p>
     *
     * @param count a int.
     */
    public void setTotalCount(int count) {
        m_totalCount = count;
    }
}
