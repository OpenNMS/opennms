/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import com.codahale.metrics.JmxReporter;
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
