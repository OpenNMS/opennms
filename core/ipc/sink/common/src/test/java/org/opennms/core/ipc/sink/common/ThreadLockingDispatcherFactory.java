/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.common;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;

public class ThreadLockingDispatcherFactory<U extends Message> extends AbstractMessageDispatcherFactory<Void> {
    private final AtomicInteger numMessageDispatched = new AtomicInteger(0);

    private final ThreadLockingSyncDispatcher<?> threadLockingSyncDispatcher = new ThreadLockingSyncDispatcher<U>() {
        @Override
        public void send(U message) {
            super.send(message);
            numMessageDispatched.incrementAndGet();
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected <S extends Message, T extends Message> SyncDispatcher<S> createSyncDispatcher(DispatcherState<Void,S,T> state) {
        return (ThreadLockingSyncDispatcher<S>)threadLockingSyncDispatcher;
    }

    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, Void metadata, T message) {
        throw new IllegalStateException();
    }

    @SuppressWarnings("unchecked")
    public <S extends Message> ThreadLockingSyncDispatcher<S> getThreadLockingSyncDispatcher() {
        return (ThreadLockingSyncDispatcher<S>)threadLockingSyncDispatcher;
    }

    public int getNumMessageDispatched() {
        return numMessageDispatched.get();
    }
}
