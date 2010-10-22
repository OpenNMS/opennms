package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsOutageCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="outages")
public class OnmsOutageCollection extends LinkedList<OnmsOutage> {

	/**
     * 
     */
    private static final long serialVersionUID = -12993787944327060L;

    /**
	 * <p>Constructor for OnmsOutageCollection.</p>
	 */
	public OnmsOutageCollection() {
        super();
    }

    /**
     * <p>Constructor for OnmsOutageCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsOutageCollection(Collection<? extends OnmsOutage> c) {
        super(c);
    }

    /**
     * <p>getNotifications</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="outage")
    public List<OnmsOutage> getNotifications() {
        return this;
    }

    /**
     * <p>setEvents</p>
     *
     * @param events a {@link java.util.List} object.
     */
    public void setEvents(List<OnmsOutage> events) {
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

