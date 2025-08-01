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
package org.opennms.core.rpc.jms;

import org.apache.camel.CamelContext;
import org.apache.camel.Converter;
import org.apache.camel.ExchangePattern;
import org.apache.camel.TypeConverters;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsEndpoint;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.rpc.camel.CamelRpcConstants;
import org.opennms.core.rpc.camel.CamelRpcServerProcessor;
import org.opennms.core.rpc.camel.CamelRpcServerRouteManager;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsRpcServerRouteManager extends CamelRpcServerRouteManager {
    private static final Logger LOG = LoggerFactory.getLogger(CamelRpcServerRouteManager.class);

    private final TracerRegistry tracerRegistry;

    public JmsRpcServerRouteManager(CamelContext context, MinionIdentity identity, TracerRegistry tracerRegistry) {
        super(context, identity);
        this.tracerRegistry = tracerRegistry;
        context.getTypeConverterRegistry().addTypeConverters(new BooleanTypeConverters());
    }

    @Override
    public RouteBuilder getRouteBuilder(CamelContext context, MinionIdentity identity, RpcModule<RpcRequest,RpcResponse> module) {
        return new DynamicRpcRouteBuilder(context, identity, module, tracerRegistry);
    }

    public static final class BooleanTypeConverters implements TypeConverters {
        @Converter
        public String toString(final boolean data) {
            return Boolean.toString(data);
        }
        @Converter
        public boolean toBoolean(final String data) {
            return Boolean.valueOf(data);
        }
    }

    private static final class DynamicRpcRouteBuilder extends RouteBuilder {
        private final MinionIdentity identity;
        private final RpcModule<RpcRequest,RpcResponse> module;
        private final JmsQueueNameFactory queueNameFactory;
        private final TracerRegistry tracerRegistry;

        private DynamicRpcRouteBuilder(CamelContext context, MinionIdentity identity,
                                       RpcModule<RpcRequest,RpcResponse> module, TracerRegistry tracerRegistry) {
            super(context);
            this.identity = identity;
            this.module = module;
            this.queueNameFactory = new JmsQueueNameFactory(CamelRpcConstants.JMS_QUEUE_PREFIX,
                    module.getId(), identity.getLocation());
            this.tracerRegistry = tracerRegistry;
            context.getTypeConverterRegistry().addTypeConverters(new BooleanTypeConverters());
        }

        public String getQueueName() {
            return queueNameFactory.getName();
        }

        @Override
        public void configure() throws Exception {
            final JmsEndpoint endpoint = getContext().getEndpoint(String.format("queuingservice:%s?asyncConsumer=true",
                    queueNameFactory.getName()), JmsEndpoint.class);

            final String selector = getJmsSelector(identity.getId());
            LOG.trace("Using JMS selector: {} for module: {} on: {}", selector, module, identity);
            endpoint.setSelector(selector);
            // Initialize Tracer registry with service name.
            tracerRegistry.init(identity.getLocation()+"@"+identity.getId());
            from(endpoint).setExchangePattern(ExchangePattern.InOut)
                    .process(new CamelRpcServerProcessor(module, tracerRegistry))
                    .routeId(getRouteId(module));
        }

    }

    static String getJmsSelector(String systemId) {
        if (systemId == null) {
            return String.format("%s IS NULL", CamelRpcConstants.JMS_SYSTEM_ID_HEADER);
        }
        return String.format("%s='%s' OR %s IS NULL",
                CamelRpcConstants.JMS_SYSTEM_ID_HEADER,
                systemId.replace("'", "\\'"),
                CamelRpcConstants.JMS_SYSTEM_ID_HEADER);
    }
}
