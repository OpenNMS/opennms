/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.osgi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is a wrapper for the "real" EventListener/EventConsumer.
 * But due to the fact that we want EventListeners (in general) be stored in the
 * OSGi-container and there could be an infinite amount of event listeners registered
 * we need a simple way of getting all event listeners which are listening to specific events.<br/><br/>
 * 
 * So the {@link org.opennms.osgi.EventListener} provides this functionality.
 * 
 * 
 * @author Markus von Rüden
 *
 */
class EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(EventListener.class);

    /**
     * The EventListener is stored in the OSGi-Container as a service. To get all listeners
     * for a specific event type, we need to set this so called "consumingType" during registration.
     * This method adds a Property "consumingType" to group the EventListeners.
     * 
     * @param consumingType Means this {@link org.opennms.osgi.EventListener} consumes Events of type 'consumingType'
     */
    public static Hashtable<String,Object> getProperties(Class<?> consumingType) {
        Hashtable<String,Object> properties = new Hashtable<String,Object>();
        properties.put(EventListener.class.getName() + ".consumingType", consumingType.getName());
        return properties;
    }
    
    /**
     * The object of the eventConsumer (any class which has one or multiple &#64;Eventconsumer annotations.
     */
    private Object eventConsumer;
    
    /**
     * The method to invoke on eventListening. This is the method with the &#64;Eventconsumer annotation.
     */
    private Method eventMethod;
    

    public void setEventConsumer(Object eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    public void setEventMethod(Method eventMethod) {
        this.eventMethod = eventMethod;
    }

    /**
     * When an event is fired, this method is invoked and then forwards the 
     * call to the saved eventMethod.
     * 
     * @param eventObject The event object (e.g. MyUiStateChangedEvent).
     */
    public void invoke(Object eventObject) {
        try {
            eventMethod.invoke(eventConsumer, eventObject);
        } catch (IllegalAccessException e) {
            LOG.error("Error while invoking event consuming method", e);
        } catch (InvocationTargetException e) {
            LOG.error("Error while invoking event consuming method", e);
        }
    }
}
