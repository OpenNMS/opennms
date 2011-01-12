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

	/**
     * 
     */
    private static final long serialVersionUID = -8540172282769996576L;
    private int m_totalCount;

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
