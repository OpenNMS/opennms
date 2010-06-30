package org.opennms.web.event;

import java.util.Date;

import org.opennms.web.event.filter.EventCriteria;

/**
 * <p>WebEventRepository interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface WebEventRepository {
    
    /**
     * <p>countMatchingEvents</p>
     *
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     * @return a int.
     */
    public abstract int countMatchingEvents(EventCriteria criteria);
    
    /**
     * <p>countMatchingEventsBySeverity</p>
     *
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     * @return an array of int.
     */
    public abstract int[] countMatchingEventsBySeverity(EventCriteria criteria);
    
    /**
     * <p>getEvent</p>
     *
     * @param eventId a int.
     * @return a {@link org.opennms.web.event.Event} object.
     */
    public abstract Event getEvent(int eventId);
    
    /**
     * <p>getMatchingEvents</p>
     *
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     * @return an array of {@link org.opennms.web.event.Event} objects.
     */
    public abstract Event[] getMatchingEvents(EventCriteria criteria);
    
    /**
     * <p>acknowledgeMatchingEvents</p>
     *
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     */
    public abstract void acknowledgeMatchingEvents(String user, Date timestamp, EventCriteria criteria);
    
    /**
     * <p>acknowledgeAll</p>
     *
     * @param user a {@link java.lang.String} object.
     * @param timestamp a java$util$Date object.
     */
    public abstract void acknowledgeAll(String user, Date timestamp);
    
    /**
     * <p>unacknowledgeMatchingEvents</p>
     *
     * @param criteria a {@link org.opennms.web.event.filter.EventCriteria} object.
     */
    public abstract void unacknowledgeMatchingEvents(EventCriteria criteria);
    
    /**
     * <p>unacknowledgeAll</p>
     */
    public abstract void unacknowledgeAll();
}
