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

import org.apache.camel.Exchange;
import org.opennms.core.ipc.common.aws.sqs.AmazonSQSManager;
import org.opennms.core.ipc.common.aws.sqs.AmazonSQSQueueException;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.rpc.camel.CamelRpcClientPreProcessor;
import org.opennms.core.rpc.camel.CamelRpcConstants;
import org.opennms.core.rpc.camel.CamelRpcRequest;

import java.util.Objects;

public class AmazonSQSClientPreProcessor extends CamelRpcClientPreProcessor {

    private final AmazonSQSManager sqsManager;

    public AmazonSQSClientPreProcessor(AmazonSQSManager sqsManager) {
        this.sqsManager = Objects.requireNonNull(sqsManager);
    }

    @Override
    public void process(Exchange exchange) throws AmazonSQSQueueException {
        @SuppressWarnings("unchecked")
        final CamelRpcRequest<RpcRequest,RpcResponse> wrapper = exchange.getIn().getBody(CamelRpcRequest.class);

        final String requestQueueName = sqsManager.getRpcRequestQueueNameAndCreateIfNecessary(
                wrapper.getModule().getId(),
                wrapper.getRequest().getLocation());
        final String replyQueueName = sqsManager.getRpcReplyQueueNameAndCreateIfNecessary(
                wrapper.getModule().getId(),
                wrapper.getRequest().getLocation());
        exchange.getIn().setHeader(CamelRpcConstants.JMS_QUEUE_NAME_HEADER, requestQueueName);
        exchange.getIn().setHeader(CamelRpcConstants.JMS_REPLY_TO_QUEUE_NAME_HEADER, replyQueueName);

        exchange.getIn().setHeader(CamelRpcConstants.CAMEL_JMS_REQUEST_TIMEOUT_HEADER, wrapper.getRequest().getTimeToLiveMs() != null ? wrapper.getRequest().getTimeToLiveMs() : CAMEL_JMS_REQUEST_TIMEOUT);
        if (wrapper.getRequest().getSystemId() != null) {
            exchange.getIn().setHeader(CamelRpcConstants.JMS_SYSTEM_ID_HEADER, wrapper.getRequest().getSystemId());
        }

        final String request = wrapper.getModule().marshalRequest((RpcRequest)wrapper.getRequest());
        exchange.getIn().setBody(request);
    }
}
