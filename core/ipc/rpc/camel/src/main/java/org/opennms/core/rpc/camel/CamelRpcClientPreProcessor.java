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
package org.opennms.core.rpc.camel;

import static org.opennms.core.rpc.api.RpcClientFactory.JMX_DOMAIN_RPC;
import static org.opennms.core.rpc.api.RpcClientFactory.RPC_REQUEST_SIZE;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.MetricRegistry;

public class CamelRpcClientPreProcessor implements Processor {
    private static final Logger LOG = LoggerFactory.getLogger(CamelRpcClientPreProcessor.class);

    public static final String CAMEL_JMS_REQUEST_TIMEOUT_PROPERTY = "org.opennms.jms.timeout";
    public static final long CAMEL_JMS_REQUEST_TIMEOUT_DEFAULT = 20000L;
    protected final Long CAMEL_JMS_REQUEST_TIMEOUT;

    private MetricRegistry metrics = new MetricRegistry();
    private JmxReporter metricsReporter = null;

    public CamelRpcClientPreProcessor() {
        long camelJmsRequestTimeout = PropertiesUtils.getProperty(System.getProperties(), CAMEL_JMS_REQUEST_TIMEOUT_PROPERTY, CAMEL_JMS_REQUEST_TIMEOUT_DEFAULT);

        if (camelJmsRequestTimeout <= 0L) {
            LOG.error("Invalid value {} for property {} - must be greater than zero!", camelJmsRequestTimeout, CAMEL_JMS_REQUEST_TIMEOUT_PROPERTY);
            camelJmsRequestTimeout = CAMEL_JMS_REQUEST_TIMEOUT_DEFAULT;
        }

        LOG.debug("Value {} set for property {}", camelJmsRequestTimeout, CAMEL_JMS_REQUEST_TIMEOUT_PROPERTY);

        CAMEL_JMS_REQUEST_TIMEOUT = camelJmsRequestTimeout;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        @SuppressWarnings("unchecked")
        final CamelRpcRequest<RpcRequest,RpcResponse> wrapper = exchange.getIn().getBody(CamelRpcRequest.class);
        final JmsQueueNameFactory queueNameFactory = new JmsQueueNameFactory(CamelRpcConstants.JMS_QUEUE_PREFIX,
                wrapper.getModule().getId(), wrapper.getRequest().getLocation());
        exchange.getIn().setHeader(CamelRpcConstants.JMS_QUEUE_NAME_HEADER, queueNameFactory.getName());
        exchange.getIn().setHeader(CamelRpcConstants.CAMEL_JMS_REQUEST_TIMEOUT_HEADER, wrapper.getRequest().getTimeToLiveMs() != null ? wrapper.getRequest().getTimeToLiveMs() : CAMEL_JMS_REQUEST_TIMEOUT);
        if (wrapper.getRequest().getSystemId() != null) {
            exchange.getIn().setHeader(CamelRpcConstants.JMS_SYSTEM_ID_HEADER, wrapper.getRequest().getSystemId());
        }
        if(wrapper.getTracingInfo().size() > 0) {
            // Message mapping between camel and JMS ignores non-primitive headers there by need for marshalling.
            String tracingInfo = TracingInfoCarrier.marshalTracingInfo(wrapper.getTracingInfo());
            if(tracingInfo != null) {
                exchange.getIn().setHeader(CamelRpcConstants.JMS_TRACING_INFO, tracingInfo);
            }
        }
        final String request = wrapper.getModule().marshalRequest((RpcRequest)wrapper.getRequest());
        exchange.getIn().setBody(request);
        final Histogram rpcRequestSize = metrics.histogram(MetricRegistry.name(wrapper.getRequest().getLocation(), wrapper.getModule().getId(), RPC_REQUEST_SIZE));
        rpcRequestSize.update(request.getBytes().length);
    }

    public void start() {
        // Initialize metrics reporter.
        metricsReporter = JmxReporter.forRegistry(metrics).
                inDomain(JMX_DOMAIN_RPC).build();
        metricsReporter.start();
    }

    public void stop() {
        if (metricsReporter != null) {
            metricsReporter.close();
        }
    }
}
