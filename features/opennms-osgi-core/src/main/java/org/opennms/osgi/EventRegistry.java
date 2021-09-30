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

import org.opennms.osgi.locator.OnmsServiceManagerLocator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * The EventRegistry is a single point of entry to get an EventListener/EventConsumer registered in the underlying OSGi-Container.
 * To register an EventConsumer, you have to invoke the method {@link #addPossibleEventConsumer(Object, VaadinApplicationContext)}.
 * 
 * @author Markus von Rüden
 *
 */
public class EventRegistry {

    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final BundleContext bundleContext;

    public EventRegistry(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Adds the possibleEventConsumer as an EventListener/EventConsumer to the
     * OSGi registry.<br/><br/>
     * 
     * The event consumer is registered when:
     * <ul>
     * <li>The object has at least one method annotated with &#64;EventConsumer</li>
     * <li>The annotated method has only one parameter.
     * </ul><br/>
     * 
     * <b>Attention:</b> If the possibleEventConsumer has no Annotation
     * &#64;EventConsumer or has multiple method parameters the object is not
     * registered as a listener! <br/>
     * 
     * The {@link VaadinApplicationContext} parameter is used to bind the event
     * consumer to the user session (otherwise all users would be informed when
     * one user provokes an event notification).
     * 
     * @param possibleEventConsumer
     *            An object with or without an annotation &#64;EventConsumer.
     * @param applicationContext
     *            must not be null. is used to bind the event consumer to the
     *            user session (otherwise all users would be informed when one
     *            user provokes an event notification).
     * @see {@link EventConsumer}, {@link EventListener}
     */
    public void addPossibleEventConsumer(Object possibleEventConsumer, VaadinApplicationContext applicationContext) {
        if (possibleEventConsumer == null) return;
        if (isPossibleEventConsumer(possibleEventConsumer)) {
            List<Method> eventConsumerMethods = getEventConsumerMethods(possibleEventConsumer.getClass());
            for (Method eachEventConsumerMethod : eventConsumerMethods) {
                boolean hasType = eachEventConsumerMethod.getParameterTypes() != null && eachEventConsumerMethod.getParameterTypes().length > 0;
                boolean hasMultipleTypes = eachEventConsumerMethod.getParameterTypes() != null && eachEventConsumerMethod.getParameterTypes().length > 1;
                if (!hasType) {
                    LOG.warn("The method '{1}' in class '{2}' is annotated as a 'EventConsumer' but does not have any event type as a parameter. Method is ignored",
                            eachEventConsumerMethod.getName(),
                            possibleEventConsumer.getClass());
                    continue;
                }
                if (hasMultipleTypes) {
                    LOG.warn("The method '{1}' in class '{2}' has multiple parameters. It must only have one parameter. Method is ignored",
                            eachEventConsumerMethod.getName(), possibleEventConsumer.getClass());
                    continue;
                }

                // create a Wrapper for the possibleEventConsumer
                EventListener listener = new EventListener();
                listener.setEventConsumer(possibleEventConsumer);
                listener.setEventMethod(eachEventConsumerMethod);

                // register as event listener for session scope
                getOnmsServiceManager().registerAsService(EventListener.class, listener, applicationContext, EventListener.getProperties(eachEventConsumerMethod.getParameterTypes()[0]));
            }
        }
    }
    
    public EventProxy getScope(VaadinApplicationContext vaadinApplicationContext) {
        return new EventProxyImpl(bundleContext, vaadinApplicationContext);
    }

    private OnmsServiceManager getOnmsServiceManager() {
        return new OnmsServiceManagerLocator().lookup(bundleContext);
    }

    private boolean isPossibleEventConsumer(Object possibleEventConsumer) {
        if (possibleEventConsumer == null) return false;
        return !getEventConsumerMethods(possibleEventConsumer.getClass()).isEmpty();
    }

    // Returns a list of all methods which have a annotation "EventConsumer"
    private List<Method> getEventConsumerMethods(Class<?> clazz) {
        List<Method> eventConsumerMethods = new ArrayList<>();
        for (Method eachMethod : clazz.getMethods()) {
            if (eachMethod.getAnnotation(EventConsumer.class) != null) {
                eventConsumerMethods.add(eachMethod);
            }
        }
        return eventConsumerMethods;
    }
}