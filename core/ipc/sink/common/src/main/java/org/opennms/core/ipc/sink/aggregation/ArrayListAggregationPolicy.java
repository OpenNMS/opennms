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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.opennms.core.ipc.sink.api.AggregationPolicy;

/**
 * An aggregation policy that creates {@link ArrayList} aggregates.
 *
 * @author Seth
 */
public class ArrayListAggregationPolicy<S> implements AggregationPolicy<S, List<S>, List<S>> {

    private final int m_completionSize;
    private final int m_completionInterval;
    private final Function<S,Object> m_keyMapper;

    public ArrayListAggregationPolicy(final int completionSize, final int completionInterval, final Function<S,Object> keyMapper) {
        m_completionSize = completionSize;
        m_completionInterval = completionInterval;
        m_keyMapper = keyMapper;
    }

    @Override
    public Object key(S message) {
        return m_keyMapper.apply(message);
    }

    @Override
    public List<S> aggregate(List<S> accumulator, S newMessage) {
        if (accumulator == null) {
            accumulator = new ArrayList<S>(m_completionSize);
        }
        accumulator.add(newMessage);
        return accumulator;
    }

    @Override
    public List<S> build(List<S> accumulator) {
        return accumulator;
    }

    @Override
    public int getCompletionSize() {
        return m_completionSize;
    }

    @Override
    public int getCompletionIntervalMs() {
        return m_completionInterval;
    }

}
