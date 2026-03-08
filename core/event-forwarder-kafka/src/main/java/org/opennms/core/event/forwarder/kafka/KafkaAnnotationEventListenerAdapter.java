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
package org.opennms.core.event.forwarder.kafka;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Discovers {@link EventHandler}-annotated methods on an
 * {@link org.opennms.netmgt.events.api.annotations.EventListener @EventListener}-annotated
 * class and registers a delegate {@link EventListener} with any
 * {@link EventSubscriptionService}.
 *
 * <p>This is a simplified, Kafka-friendly counterpart to
 * {@link org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter},
 * which is tightly coupled to {@code EventIpcManager}. This adapter works
 * with any {@link EventSubscriptionService} implementation, including
 * {@link KafkaEventSubscriptionService}.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * @EventListener(name = "MyDaemon")
 * public class MyDaemon {
 *     @EventHandler(uei = "uei.opennms.org/nodes/nodeDown")
 *     public void onNodeDown(IEvent event) { ... }
 * }
 *
 * var adapter = new KafkaAnnotationEventListenerAdapter(myDaemon, subscriptionService);
 * adapter.afterPropertiesSet();
 * }</pre>
 */
public class KafkaAnnotationEventListenerAdapter implements EventListener, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAnnotationEventListenerAdapter.class);

    private final Object annotatedListener;
    private final EventSubscriptionService subscriptionService;

    private String name;
    private String logPrefix;
    private final Map<String, Method> ueiToHandlerMap = new HashMap<>();

    /**
     * Creates a new adapter for the given annotated listener object.
     *
     * @param annotatedListener an object whose class is annotated with
     *        {@link org.opennms.netmgt.events.api.annotations.EventListener @EventListener}
     *        and has methods annotated with {@link EventHandler}
     * @param subscriptionService the subscription service to register with
     */
    public KafkaAnnotationEventListenerAdapter(
            Object annotatedListener,
            EventSubscriptionService subscriptionService) {
        this.annotatedListener = Objects.requireNonNull(annotatedListener, "annotatedListener must not be null");
        this.subscriptionService = Objects.requireNonNull(subscriptionService, "subscriptionService must not be null");
    }

    @Override
    public void afterPropertiesSet() {
        org.opennms.netmgt.events.api.annotations.EventListener listenerAnnotation =
                annotatedListener.getClass().getAnnotation(
                        org.opennms.netmgt.events.api.annotations.EventListener.class);

        if (listenerAnnotation == null) {
            throw new IllegalStateException(
                    "Class " + annotatedListener.getClass().getName()
                            + " must be annotated with @"
                            + org.opennms.netmgt.events.api.annotations.EventListener.class.getName());
        }

        name = listenerAnnotation.name();
        logPrefix = listenerAnnotation.logPrefix();
        if (logPrefix == null || logPrefix.isEmpty()) {
            logPrefix = name;
        }

        discoverHandlers();
        registerWithSubscriptionService();

        LOG.info("KafkaAnnotationEventListenerAdapter '{}' registered for {} UEI(s): {}",
                name, ueiToHandlerMap.size(), ueiToHandlerMap.keySet());
    }

    /**
     * Iterates public methods on the annotated listener class, finds those
     * annotated with {@link EventHandler}, and populates the UEI-to-Method map.
     */
    private void discoverHandlers() {
        for (Method method : annotatedListener.getClass().getMethods()) {
            EventHandler handlerAnnotation = method.getAnnotation(EventHandler.class);
            if (handlerAnnotation == null) {
                continue;
            }

            validateHandlerMethod(method);

            // Handle the single-uei attribute
            String singleUei = handlerAnnotation.uei();
            if (singleUei != null && !singleUei.isEmpty()) {
                if (ueiToHandlerMap.containsKey(singleUei)) {
                    throw new IllegalStateException(
                            "Duplicate handler for UEI '" + singleUei + "': method "
                                    + method + " conflicts with " + ueiToHandlerMap.get(singleUei));
                }
                ueiToHandlerMap.put(singleUei, method);
            }

            // Handle the multi-uei attribute
            String[] ueis = handlerAnnotation.ueis();
            if (ueis != null && ueis.length > 0) {
                for (String uei : ueis) {
                    if (ueiToHandlerMap.containsKey(uei)) {
                        throw new IllegalStateException(
                                "Duplicate handler for UEI '" + uei + "': method "
                                        + method + " conflicts with " + ueiToHandlerMap.get(uei));
                    }
                    ueiToHandlerMap.put(uei, method);
                }
            }
        }

        if (ueiToHandlerMap.isEmpty()) {
            throw new IllegalStateException(
                    "Annotated listener " + annotatedListener.getClass().getName()
                            + " has no @EventHandler-annotated methods");
        }
    }

    /**
     * Validates that a handler method has exactly one parameter of type
     * {@link IEvent} (or a supertype).
     */
    private static void validateHandlerMethod(Method method) {
        if (method.getParameterTypes().length != 1) {
            throw new IllegalStateException(
                    "EventHandler method " + method + " must take exactly one parameter");
        }
        if (!method.getParameterTypes()[0].isAssignableFrom(IEvent.class)) {
            throw new IllegalStateException(
                    "EventHandler method " + method
                            + " parameter must be assignable from IEvent");
        }
    }

    /**
     * Registers this adapter as an {@link EventListener} with the subscription
     * service for all discovered UEIs.
     */
    private void registerWithSubscriptionService() {
        subscriptionService.addEventListener(this, new HashSet<>(ueiToHandlerMap.keySet()));
    }

    // -------- EventListener implementation --------

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void onEvent(IEvent event) {
        if (event.getUei() == null) {
            LOG.debug("Ignoring event with null UEI");
            return;
        }

        Method handler = ueiToHandlerMap.get(event.getUei());
        if (handler == null) {
            LOG.debug("No handler for UEI '{}' in listener '{}'", event.getUei(), name);
            return;
        }

        try {
            handler.invoke(annotatedListener, event);
        } catch (IllegalAccessException e) {
            throw new UndeclaredThrowableException(e,
                    "Cannot invoke handler for UEI '" + event.getUei() + "' on listener '" + name + "'");
        } catch (InvocationTargetException e) {
            LOG.warn("Handler for UEI '{}' on listener '{}' threw exception: {}",
                    event.getUei(), name, e.getCause().getMessage(), e.getCause());
        }
    }
}
