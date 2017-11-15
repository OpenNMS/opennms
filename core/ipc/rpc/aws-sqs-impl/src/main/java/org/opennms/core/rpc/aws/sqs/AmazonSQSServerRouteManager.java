/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.aws.sqs;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsEndpoint;
import org.opennms.core.ipc.common.aws.sqs.AmazonSQSManager;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.rpc.camel.CamelRpcServerRouteManager;
import org.opennms.minion.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class AmazonSQSServerRouteManager extends CamelRpcServerRouteManager {
    private static final Logger LOG = LoggerFactory.getLogger(CamelRpcServerRouteManager.class);

    private final AmazonSQSManager sqsManager;

    public AmazonSQSServerRouteManager(CamelContext context, MinionIdentity identity, AmazonSQSManager sqsManager) {
        super(context, identity);
        this.sqsManager = Objects.requireNonNull(sqsManager);
    }

    @Override
    public RouteBuilder getRouteBuilder(CamelContext context, MinionIdentity identity, RpcModule<RpcRequest, RpcResponse> module) {
        return new DynamicRpcRouteBuilder(sqsManager, context, identity, module);
    }

    private static final class DynamicRpcRouteBuilder extends RouteBuilder {
        private final AmazonSQSManager sqsManager;
        private final MinionIdentity identity;
        private final RpcModule<RpcRequest,RpcResponse> module;

        private DynamicRpcRouteBuilder(AmazonSQSManager sqsManager, CamelContext context, MinionIdentity identity, RpcModule<RpcRequest,RpcResponse> module) {
            super(context);
            this.sqsManager = sqsManager;
            this.identity = identity;
            this.module = module;
        }

        @Override
        public void configure() throws Exception {
            final String requestQueueName = sqsManager.getRpcRequestQueueNameAndCreateIfNecessary(module.getId(), identity.getLocation());
            final JmsEndpoint endpoint = getContext().getEndpoint(String.format("jms:queue:%s?connectionFactory=#connectionFactory"
                            + "&correlationProperty=%s"
                            + "&asyncConsumer=true", requestQueueName,
                    AmazonSQSRPCConstants.AWS_SQS_CORRELATION_ID_HEADER), JmsEndpoint.class);

            from(endpoint).setExchangePattern(ExchangePattern.InOut)
                    .process(new SystemIdFilterProcessor(identity.getId()))
                    .process(new AmazonSQSServerProcessor(module))
                    .routeId(getRouteId(module));
        }
    }
}
