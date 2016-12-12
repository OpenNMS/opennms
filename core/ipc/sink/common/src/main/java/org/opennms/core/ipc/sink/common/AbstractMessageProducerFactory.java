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
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageProducer;
import org.opennms.core.ipc.sink.api.MessageProducerFactory;
import org.opennms.core.ipc.sink.api.SinkModule;

public abstract class AbstractMessageProducerFactory<W> implements MessageProducerFactory {

    public abstract <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, W metadata, T message);

    public <S extends Message, T extends Message> W getModuleMetadata(SinkModule<S, T> module) {
        return null;
    }

    @Override
    public <S extends Message, T extends Message> MessageProducer<S> getProducer(SinkModule<S, T> module) {
        final W metadata = getModuleMetadata(module);
        if (module.getAggregationPolicy() != null) {
            return new AggregatingMessageProducer<S,T>(module) {
                @Override
                public void dispatch(T message) {
                    AbstractMessageProducerFactory.this.dispatch(module, metadata, message);
                }
            };
        } else {
            // No aggregation strategy is set, dispatch directly to reduce overhead
            return new DirectMessageProducer<>(module, metadata);
        }
    }

    private class DirectMessageProducer<S extends Message, T extends Message> implements MessageProducer<S> {
        private final SinkModule<S, T> module;
        private final W metadata;

        public DirectMessageProducer(SinkModule<S, T> module, W metadata) {
            this.module = module;
            this.metadata = metadata;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void send(S message) {
            // Cast S to T, modules that do not use an AggregationPolicty
            // must have the same types for S and T
            AbstractMessageProducerFactory.this.dispatch(module, metadata, (T)message);
        }

        @Override
        public void close() {
            // pass
        }
    }
}
