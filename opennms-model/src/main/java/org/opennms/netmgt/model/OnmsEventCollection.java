package org.opennms.netmgt.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>OnmsEventCollection class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="events")
public class OnmsEventCollection extends LinkedList<OnmsEvent> {

	private static final long serialVersionUID = 1L;
	private int m_totalCount;

	/**
	 * <p>Constructor for OnmsEventCollection.</p>
	 */
	public OnmsEventCollection() {
        super();
    }

    /**
     * <p>Constructor for OnmsEventCollection.</p>
     *
     * @param c a {@link java.util.Collection} object.
     */
    public OnmsEventCollection(Collection<? extends OnmsEvent> c) {
        super(c);
    }

    /**
     * <p>getEvents</p>
     *
     * @return a {@link java.util.List} object.
     */
    @XmlElement(name="event")
    public List<OnmsEvent> getEvents() {
        return this;
    }

    /**
     * <p>setEvents</p>
     *
     * @param events a {@link java.util.List} object.
     */
    public void setEvents(List<OnmsEvent> events) {
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

