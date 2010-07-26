package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsAcknowledgmentCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="acknowledgments")
public class OnmsAcknowledgmentCollection extends LinkedList<OnmsAcknowledgment> {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>Constructor for OnmsAcknowledgmentCollection.</p>
	 */
	public OnmsAcknowledgmentCollection() {
        super();
    }

    /**
     * <p>Constructor for OnmsAcknowledgmentCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsAcknowledgmentCollection(Collection<? extends OnmsAcknowledgment> c) {
        super(c);
    }

    /**
     * <p>getNotifications</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="onmsAcknowledgment")
    public List<OnmsAcknowledgment> getNotifications() {
        return this;
    }

    /**
     * <p>setEvents</p>
     *
     * @param events a {@link java.util.List} object.
     */
    public void setEvents(List<OnmsAcknowledgment> events) {
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

