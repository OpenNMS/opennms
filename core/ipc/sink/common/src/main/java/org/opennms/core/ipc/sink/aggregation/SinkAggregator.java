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

import java.util.Objects;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;

/**
 * This is a specific aggregator where the messsages implement
 * the {@link Message} marker interface.
 *
 * @author jwhite
 *
 * @param <S> individual message
 * @param <T> aggregated message (i.e. bucket)
 */
public class SinkAggregator<S extends Message, T extends Message> extends Aggregator<S,T> {

    public SinkAggregator(SinkModule<S, T> module, AggregatingSinkMessageProducer<S,T> messageProducer) {
        super(Objects.requireNonNull(module).getId(), module.getAggregationPolicy(), messageProducer);
    }
}
