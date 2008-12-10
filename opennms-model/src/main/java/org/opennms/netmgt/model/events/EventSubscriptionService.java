package org.opennms.netmgt.model.events;

import java.util.Collection;

public interface EventSubscriptionService {
    
    /**
     * Registers an event listener that is interested in all events
     */
    public void addEventListener(EventListener listener);

    /**
     * Registers an event listener interested in the UEIs in the passed list
     */
    public void addEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Registers an event listener interested in the passed UEI
     */
    public void addEventListener(EventListener listener, String uei);

    /**
     * Removes a registered event listener
     */
    public void removeEventListener(EventListener listener);

    /**
     * Removes a registered event listener - the UEI list indicates the list of
     * events the listener is no more interested in
     */
    public void removeEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Removes a registered event listener - the UEI indicates an event the
     * listener is no more interested in
     */
    public void removeEventListener(EventListener listener, String uei);


}
