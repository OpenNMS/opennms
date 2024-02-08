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
package org.opennms.core.ipc.sink.api;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * Handles dispatching of messages to the registered consumer(s).
 *
 * @author jwhite
 */
public interface MessageConsumerManager {

    static final String LOG_PREFIX = "ipc";
    static final String METRIC_MESSAGES_RECEIVED = "messagesReceived";
    static final String METRIC_MESSAGE_SIZE = "messageSize";
    static final String METRIC_DISPATCH_TIME = "dispatchTime";

    <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, T message);

    <S extends Message, T extends Message> void registerConsumer(MessageConsumer<S, T> consumer) throws Exception;

    <S extends Message, T extends Message> void unregisterConsumer(MessageConsumer<S, T> consumer) throws Exception;

    static void updateMessageSize(MetricRegistry metricRegistry, String location, String moduleId, int messageSize) {
        Histogram messageSizeHistogram = metricRegistry.histogram(MetricRegistry.name(location, moduleId, METRIC_MESSAGE_SIZE));
        messageSizeHistogram.update(messageSize);
    }

    static Timer getDispatchTimerMetric(MetricRegistry metricRegistry, String location, String moduleId) {
        return metricRegistry.timer(MetricRegistry.name(location, moduleId, METRIC_DISPATCH_TIME));
    }

}
