package org.opennms.netmgt.eventd;

import org.opennms.netmgt.xml.event.Log;

public interface EventHandler {
    /**
     * Create a Runnable to handle the passed-in event Log.
     * 
     * @param eventLog events to be processed
     * @return a ready-to-run Runnable that will process the events
     */
    public abstract Runnable createRunnable(Log eventLog);
}