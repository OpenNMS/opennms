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

import org.opennms.core.ipc.sink.aggregation.AggregatingMessageProducer;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;

public abstract class AbstractMessageDispatcherFactory<W> implements MessageDispatcherFactory {

    public abstract <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, W metadata, T message);

    public <S extends Message, T extends Message> W getModuleMetadata(SinkModule<S, T> module) {
        return null;
    }

    @Override
    public <S extends Message, T extends Message> SyncDispatcher<S> createSyncDispatcher(SinkModule<S, T> module) {
        final W metadata = getModuleMetadata(module);
        if (module.getAggregationPolicy() != null) {
            return new AggregatingMessageProducer<S,T>(module) {
                @Override
                public void dispatch(T message) {
                    AbstractMessageDispatcherFactory.this.dispatch(module, metadata, message);
                }
            };
        } else {
            // No aggregation strategy is set, dispatch directly to reduce overhead
            return new DirectDispatcher<>(module, metadata);
        }
    }

    @Override
    public <S extends Message, T extends Message> AsyncDispatcher<S> createAsyncDispatcher(SinkModule<S, T> module, AsyncPolicy asyncPolicy) {
        final SyncDispatcher<S> syncDispatcher = createSyncDispatcher(module);
        return new AsyncDispatcherImpl<>(module, asyncPolicy, syncDispatcher);
    }

    private class DirectDispatcher<S extends Message, T extends Message> implements SyncDispatcher<S> {
        private final SinkModule<S, T> module;
        private final W metadata;

        public DirectDispatcher(SinkModule<S, T> module, W metadata) {
            this.module = module;
            this.metadata = metadata;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void send(S message) {
            // Cast S to T, modules that do not use an AggregationPolicty
            // must have the same types for S and T
            AbstractMessageDispatcherFactory.this.dispatch(module, metadata, (T)message);
        }

        @Override
        public void close() {
            // pass
        }
    }

}
