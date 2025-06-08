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
package org.opennms.core.ipc.twin.common;


import com.codahale.metrics.MetricRegistry;
import org.opennms.core.ipc.twin.api.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.api.TwinRequest;
import org.opennms.core.ipc.twin.api.TwinUpdate;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.Identity;

public class LocalTwinSubscriberImpl extends AbstractTwinSubscriber implements LocalTwinSubscriber {

    public LocalTwinSubscriberImpl(final Identity identity, TracerRegistry tracerRegistry, MetricRegistry metricRegistry) {
        super(identity, tracerRegistry, metricRegistry);
    }

    public  LocalTwinSubscriberImpl(final Identity identity) {
        super(identity, null, null);
    }

    @Override
    protected void sendRpcRequest(TwinRequest twinRequest) {
        // no need to send any RPC on local subscriber.
    }

    @Override
    public void accept(TwinUpdate twinUpdate) {
        super.accept(twinUpdate);
    }

    @Override
    public TracerRegistry getTracerRegistry() {
        return super.getTracerRegistry();
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return super.getMetrics();
    }
}


