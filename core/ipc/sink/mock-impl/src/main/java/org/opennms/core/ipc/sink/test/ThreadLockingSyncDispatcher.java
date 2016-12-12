package org.opennms.core.ipc.sink.test;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.opennms.test.ThreadLocker;

/**
 * This {@link SyncDispatcher} is used to verify the number of threads
 * that are producing messages.
 *
 * @author jwhite
 */
public class ThreadLockingSyncDispatcher<S extends Message> extends ThreadLocker implements SyncDispatcher<S> {
    @Override
    public void send(S message) {
        park();
    }

    @Override
    public void close() throws Exception {
        // pass
    }
}
