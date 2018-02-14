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

package org.opennms.core.ipc.sink.aggregation;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.MessageDispatcher;
import org.opennms.core.ipc.sink.api.SinkModule;

/**
 * A {@link MessageDispatcher} that applies the {@link SinkModule}'s {@link AggregationPolicy}
 * using the {@link Aggregator}.
 *
 * @author jwhite
 */
public abstract class AggregatingMessageProducer<S, T> implements MessageDispatcher<S> {

    private final Aggregator<S,T> aggregator;

    public AggregatingMessageProducer(String id, AggregationPolicy<S,T,?> policy) {
        aggregator = new Aggregator<S,T>(id, policy, this);
    }

    @Override
    public void send(S message) {
        final T log = aggregator.aggregate(message);
        if (log != null) {
            // This log is ready to be dispatched
            dispatch(log);
        }
    }

    public abstract void dispatch(T message);

    @Override
    public void close() throws Exception {
        aggregator.close();
    }
}
