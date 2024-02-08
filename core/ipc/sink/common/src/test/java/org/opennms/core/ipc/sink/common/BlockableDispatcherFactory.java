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
package org.opennms.core.ipc.sink.common;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.api.SyncDispatcher;
import org.osgi.framework.BundleContext;

import com.codahale.metrics.MetricRegistry;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

public class BlockableDispatcherFactory<U extends Message> extends AbstractMessageDispatcherFactory<Void> {

    private final BlockableSyncDispatcher<?> blockableSyncDispatcher = new BlockableSyncDispatcher<U>();

    @SuppressWarnings("unchecked")
    @Override
    protected <S extends Message, T extends Message> SyncDispatcher<S> createSyncDispatcher(DispatcherState<Void, S,
            T> state) {
        return (BlockableSyncDispatcher<S>) blockableSyncDispatcher;
    }

    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, Void metadata, T message) {
        throw new IllegalStateException();
    }

    @Override
    public String getMetricDomain() {
        return BlockableDispatcherFactory.class.getPackage().getName();
    }

    @Override
    public BundleContext getBundleContext() {
        return null;
    }

    @Override
    public Tracer getTracer() {
        return GlobalTracer.get();
    }

    @Override
    public MetricRegistry getMetrics() {
        return new MetricRegistry();
    }

    @SuppressWarnings("unchecked")
    public <S extends Message> BlockableSyncDispatcher<S> getBlockableSyncDispatcher() {
        return (BlockableSyncDispatcher<S>) blockableSyncDispatcher;
    }

}
