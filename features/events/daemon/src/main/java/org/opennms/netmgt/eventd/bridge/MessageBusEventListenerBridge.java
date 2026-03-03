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
package org.opennms.netmgt.eventd.bridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.core.messagebus.MessageHandler;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.ImmutableEvent;
import org.opennms.netmgt.events.api.model.ImmutableParm;
import org.opennms.netmgt.events.api.model.ImmutableValue;
import org.opennms.netmgt.events.api.model.IParm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges the MessageBus to {@code @EventHandler}-annotated beans.
 *
 * For each internal UEI ({@code uei.opennms.org/internal/...}) found in the
 * target bean's {@code @EventHandler} annotations, this bridge subscribes to
 * the corresponding MessageBus message type and converts incoming
 * {@link IpcMessage}s into synthetic {@link IEvent}s for delivery to the
 * annotated method.
 *
 * Non-internal UEIs are ignored — they continue to flow through
 * {@code EventIpcManager} as fault events.
 */
public class MessageBusEventListenerBridge {

    private static final Logger LOG = LoggerFactory.getLogger(MessageBusEventListenerBridge.class);

    static final String INTERNAL_UEI_PREFIX = "uei.opennms.org/internal/";

    private final MessageBus messageBus;

    /** Tracks handlers registered per bean so unregister can remove them. */
    private final Map<Object, List<MessageHandler>> handlersByBean = new HashMap<>();

    public MessageBusEventListenerBridge(MessageBus messageBus) {
        this.messageBus = Objects.requireNonNull(messageBus);
    }

    /**
     * Scans the target bean for {@code @EventHandler} annotations and subscribes
     * to the MessageBus for each internal UEI found.
     *
     * @param bean an object annotated with {@code @EventListener} and containing
     *             methods annotated with {@code @EventHandler}
     * @throws IllegalArgumentException if the bean lacks the {@code @EventListener} annotation
     */
    public void register(Object bean) {
        Objects.requireNonNull(bean, "bean must not be null");

        EventListener listenerInfo = bean.getClass().getAnnotation(EventListener.class);
        if (listenerInfo == null) {
            throw new IllegalArgumentException(
                    bean.getClass().getName() + " must be annotated with @EventListener");
        }

        List<MessageHandler> handlers = new ArrayList<>();

        for (Method method : bean.getClass().getMethods()) {
            EventHandler handlerInfo = method.getAnnotation(EventHandler.class);
            if (handlerInfo == null) {
                continue;
            }

            List<String> ueis = collectUeis(handlerInfo);
            for (String uei : ueis) {
                if (!uei.startsWith(INTERNAL_UEI_PREFIX)) {
                    continue;
                }

                String messageType = uei.substring(INTERNAL_UEI_PREFIX.length());
                BridgeMessageHandler handler = new BridgeMessageHandler(
                        listenerInfo.name() + "." + method.getName(),
                        bean, method, uei);
                messageBus.subscribe(messageType, handler);
                handlers.add(handler);

                LOG.debug("Bridged MessageBus type '{}' → {}.{}() for UEI {}",
                        messageType, bean.getClass().getSimpleName(), method.getName(), uei);
            }
        }

        if (!handlers.isEmpty()) {
            handlersByBean.put(bean, handlers);
        }
    }

    /**
     * Unsubscribes all MessageBus handlers previously registered for this bean.
     */
    public void unregister(Object bean) {
        List<MessageHandler> handlers = handlersByBean.remove(bean);
        if (handlers != null) {
            for (MessageHandler handler : handlers) {
                messageBus.unsubscribe(handler);
            }
        }
    }

    private static List<String> collectUeis(EventHandler handlerInfo) {
        List<String> ueis = new ArrayList<>();
        String singleUei = handlerInfo.uei();
        if (singleUei != null && !singleUei.isEmpty()) {
            ueis.add(singleUei);
        }
        String[] multiUeis = handlerInfo.ueis();
        if (multiUeis != null) {
            for (String uei : multiUeis) {
                ueis.add(uei);
            }
        }
        return ueis;
    }

    /**
     * Converts an {@link IpcMessage} to a synthetic {@link IEvent} using the
     * immutable builder, preserving the original UEI, source, node, interface,
     * timestamp, and parameters.
     */
    static IEvent toSyntheticEvent(IpcMessage message, String uei) {
        List<IParm> parms = new ArrayList<>();
        for (Map.Entry<String, String> entry : message.getParameters().entrySet()) {
            parms.add(ImmutableParm.newBuilder()
                    .setParmName(entry.getKey())
                    .setValue(ImmutableValue.newBuilder()
                            .setContent(entry.getValue())
                            .setType("string")
                            .build())
                    .build());
        }

        ImmutableEvent.Builder builder = ImmutableEvent.newBuilder()
                .setUei(uei)
                .setSource(message.getSource())
                .setNodeid(message.getNodeId())
                .setTime(new Date(message.getTimestamp()))
                .setCreationTime(new Date(message.getTimestamp()))
                .setParms(parms);

        if (message.getInterfaceAddress() != null) {
            builder.setInterface(message.getInterfaceAddress());
            try {
                builder.setInterfaceAddress(InetAddress.getByName(message.getInterfaceAddress()));
            } catch (UnknownHostException e) {
                // Interface string is set; address resolution is best-effort
                LOG.debug("Could not resolve interface address '{}'", message.getInterfaceAddress());
            }
        }

        return builder.build();
    }

    private static class BridgeMessageHandler implements MessageHandler {

        private final String name;
        private final Object bean;
        private final Method method;
        private final String uei;

        BridgeMessageHandler(String name, Object bean, Method method, String uei) {
            this.name = name;
            this.bean = bean;
            this.method = method;
            this.uei = uei;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void onMessage(IpcMessage message) {
            IEvent syntheticEvent = toSyntheticEvent(message, uei);
            try {
                method.invoke(bean, syntheticEvent);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Cannot access " + method, e);
            } catch (InvocationTargetException e) {
                LOG.error("Exception in bridged handler {}", name, e.getCause());
                throw new RuntimeException("Exception in " + method, e.getCause());
            }
        }
    }
}
