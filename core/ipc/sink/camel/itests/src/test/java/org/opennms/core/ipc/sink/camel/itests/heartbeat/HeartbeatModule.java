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
package org.opennms.core.ipc.sink.camel.itests.heartbeat;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.xml.AbstractXmlSinkModule;

public class HeartbeatModule extends AbstractXmlSinkModule<Heartbeat, Heartbeat> {

    public static final HeartbeatModule INSTANCE = new HeartbeatModule();

    public HeartbeatModule() {
        super(Heartbeat.class);
    }

    @Override
    public int getNumConsumerThreads() {
        return 1;
    }

    @Override
    public String getId() {
        return "Heartbeat";
    }

    @Override
    public AggregationPolicy<Heartbeat, Heartbeat, Heartbeat> getAggregationPolicy() {
        // No aggregation
        return null;
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        // Only synchronous dispatching
        return null;
    }

}
