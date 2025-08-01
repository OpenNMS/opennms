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
