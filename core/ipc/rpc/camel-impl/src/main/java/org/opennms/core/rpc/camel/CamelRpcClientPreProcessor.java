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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.camel.JmsQueueNameFactory;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;

public class CamelRpcClientPreProcessor implements Processor {

    @Override
    public void process(Exchange exchange) {
        @SuppressWarnings("unchecked")
        final CamelRpcRequest<RpcRequest,RpcResponse> wrapper = exchange.getIn().getBody(CamelRpcRequest.class);
        final JmsQueueNameFactory queueNameFactory = new JmsQueueNameFactory(CamelRpcConstants.JMS_QUEUE_PREFIX,
                wrapper.getModule().getId(), wrapper.getRequest().getLocation());
        exchange.getIn().setHeader(CamelRpcConstants.JMS_QUEUE_NAME_HEADER, queueNameFactory.getName());
        exchange.getIn().setHeader(CamelRpcConstants.CAMEL_JMS_REQUEST_TIMEOUT_HEADER, wrapper.getRequest().getTimeToLiveMs());
        final String request = wrapper.getModule().marshalRequest((RpcRequest)wrapper.getRequest());
        exchange.getIn().setBody(request);
    }
}
