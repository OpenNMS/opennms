package org.opennms.netmgt.model.events;

import java.util.Collection;

/**
 * <p>EventSubscriptionService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EventSubscriptionService {
    
    /**
     * Registers an event listener that is interested in all events
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     */
    public void addEventListener(EventListener listener);

    /**
     * Registers an event listener interested in the UEIs in the passed list
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param ueis a {@link java.util.Collection} object.
     */
    public void addEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Registers an event listener interested in the passed UEI
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void addEventListener(EventListener listener, String uei);

    /**
     * Removes a registered event listener
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     */
    public void removeEventListener(EventListener listener);

    /**
     * Removes a registered event listener - the UEI list indicates the list of
     * events the listener is no more interested in
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param ueis a {@link java.util.Collection} object.
     */
    public void removeEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Removes a registered event listener - the UEI indicates an event the
     * listener is no more interested in
     *
     * @param listener a {@link org.opennms.netmgt.model.events.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void removeEventListener(EventListener listener, String uei);


}
