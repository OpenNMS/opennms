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
import org.opennms.core.ipc.sink.api.Message;

/**
 * An aggregation policy that performs a simple map operation
 * on the given message.
 *
 * @author jwhite
 */
public abstract class MappingAggregationPolicy<S, T extends Message> implements AggregationPolicy<S, T, T> {

    public abstract T map(S message);

    @Override
    public Object key(S message) {
        return message;
    }

    @Override
    public int getCompletionSize() {
        return 1;
    }

    @Override
    public int getCompletionIntervalMs() {
        return 0;
    }

    @Override
    public T aggregate(T oldBucket, S newMessage) {
        return map(newMessage);
    }

    @Override
    public T build(T accumulator) {
        return accumulator;
    }

}
