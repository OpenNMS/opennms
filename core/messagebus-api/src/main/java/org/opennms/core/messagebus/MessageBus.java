package org.opennms.core.messagebus;

import java.util.Collection;

public interface MessageBus {
    void publish(IpcMessage message);
    void subscribe(String messageType, MessageHandler handler);
    void subscribe(Collection<String> messageTypes, MessageHandler handler);
    void unsubscribe(MessageHandler handler);
}
