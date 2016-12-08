package org.opennms.core.ipc.sink.test;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageProducer;
import org.opennms.test.ThreadLocker;

/**
 * This {@link MessageProducer} is used to verify the number of threads
 * that are producing messages.
 *
 * @author jwhite
 */
public class ThreadLockingMessageProducer<T extends Message> extends ThreadLocker implements MessageProducer<T> {
    @Override
    public void send(T message) {
        park();
    }
}
