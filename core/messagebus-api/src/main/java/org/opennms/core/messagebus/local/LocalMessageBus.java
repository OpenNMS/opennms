package org.opennms.core.messagebus.local;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opennms.core.messagebus.IpcMessage;
import org.opennms.core.messagebus.MessageBus;
import org.opennms.core.messagebus.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalMessageBus implements MessageBus {

    private static final Logger LOG = LoggerFactory.getLogger(LocalMessageBus.class);

    private final Map<String, List<MessageHandler>> handlersByType = new ConcurrentHashMap<>();

    @Override
    public void publish(IpcMessage message) {
        List<MessageHandler> handlers = handlersByType.get(message.getType());
        if (handlers != null) {
            for (MessageHandler handler : handlers) {
                try {
                    handler.onMessage(message);
                } catch (Exception e) {
                    LOG.warn("Handler {} failed processing message type {}",
                            handler.getName(), message.getType(), e);
                }
            }
        }
    }

    @Override
    public void subscribe(String messageType, MessageHandler handler) {
        handlersByType.computeIfAbsent(messageType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    @Override
    public void subscribe(Collection<String> messageTypes, MessageHandler handler) {
        for (String type : messageTypes) {
            subscribe(type, handler);
        }
    }

    @Override
    public void unsubscribe(MessageHandler handler) {
        handlersByType.values().forEach(list -> list.remove(handler));
    }
}
