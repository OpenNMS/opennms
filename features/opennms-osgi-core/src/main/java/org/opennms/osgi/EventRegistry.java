/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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