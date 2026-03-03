package org.opennms.core.messagebus;

public interface MessageHandler {
    String getName();
    void onMessage(IpcMessage message);
}
