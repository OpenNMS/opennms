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
