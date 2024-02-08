/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
