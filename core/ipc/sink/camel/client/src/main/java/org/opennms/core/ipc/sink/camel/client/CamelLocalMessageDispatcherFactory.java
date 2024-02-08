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
package org.opennms.core.ipc.sink.camel.client;

import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_PRODUCER_DOMAIN;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.camel.server.CamelMessageConsumerManager;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.tracing.api.TracerRegistry;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.codahale.metrics.MetricRegistry;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/**
 * Message producer that dispatches the messages directly the consumers.
 *
 * @author jwhite
 */
public class CamelLocalMessageDispatcherFactory extends AbstractMessageDispatcherFactory<Void> implements InitializingBean, DisposableBean {

    @Autowired
    private CamelMessageConsumerManager messageConsumerManager;

    @Autowired
    private TracerRegistry tracerRegistry;

    private MetricRegistry metrics;

    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, Void metadata, T message) {
        messageConsumerManager.dispatch(module, message);
    }

    @Override
    public String getMetricDomain() {
        return SINK_METRIC_PRODUCER_DOMAIN;
    }

    @Override
    public BundleContext getBundleContext() {
        return null;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    @Override
    public Tracer getTracer() {
        if (getTracerRegistry() != null) {
            return getTracerRegistry().getTracer();
        }
        return GlobalTracer.get();
    }

    @Override
    public void afterPropertiesSet() {
        onInit();
    }

    @Override
    public void destroy() {
        onDestroy();
    }

    @Override
    public MetricRegistry getMetrics() {
        if(metrics == null) {
            metrics = new MetricRegistry();
        }
        return metrics;
    }

    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }
}
