/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.twin.jms.subscriber;

import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Synchronization;
import org.opennms.core.ipc.twin.common.AbstractTwinSubscriber;
import org.opennms.core.ipc.twin.common.TwinRequestBean;
import org.opennms.core.ipc.twin.common.TwinResponseBean;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.distributed.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsTwinSubscriber extends AbstractTwinSubscriber implements Processor {

    private static final String TWIN_QUEUE_NAME_FORMAT = "%s.%s";
    private static final Logger LOG = LoggerFactory.getLogger(JmsTwinSubscriber.class);

    @EndpointInject(uri = "direct:twinRpc", context = "twinRpcClient")
    private ProducerTemplate template;

    @EndpointInject(uri = "direct:twinRpc", context = "twinRpcClient")
    private Endpoint endpoint;

    /*
       Two blueprint camel contexts couldn't be started from the same blueprint
     */
    private final CamelContext sinkCamelContext = new DefaultCamelContext();

    public JmsTwinSubscriber(MinionIdentity minionIdentity, Component queuingservice, String debugMaxChar) {
        super(minionIdentity);
        sinkCamelContext.addComponent("queuingservice", queuingservice);
        sinkCamelContext.getGlobalOptions().put(Exchange.LOG_DEBUG_BODY_MAX_CHARS, debugMaxChar);
    }

    @Override
    protected void sendRpcRequest(TwinRequestBean twinRequest) {
        TwinRequestProto twinRequestProto = mapTwinRequestToProto(twinRequest);
        template.asyncCallbackSendBody(endpoint, twinRequestProto.toByteArray(), new Synchronization() {
            @Override
            public void onComplete(Exchange exchange) {
                try {
                    byte[] response = exchange.getOut().getBody(byte[].class);
                    accept(mapTwinResponseToProto(response));
                } catch (Exception e) {
                    LOG.error("Failed to process response", e);
                }
            }

            @Override
            public void onFailure(Exchange exchange) {
                // Nothing to do when there is a failure as we don't have any timeouts here.
            }
        });
    }

    public void init() throws Exception {
        sinkCamelContext.addRoutes(new SinkRouteBuilder(this));
        sinkCamelContext.start();
        LOG.info("JMS Twin subscriber initialized");
    }

    public void destroy() throws Exception {
        sinkCamelContext.stop();
        LOG.info("JMS Twin subscriber stopped");
    }

    private TwinRequestProto mapTwinRequestToProto(TwinRequestBean twinRequest) {
        TwinRequestProto.Builder builder = TwinRequestProto.newBuilder();
        builder.setConsumerKey(twinRequest.getKey()).setLocation(getMinionIdentity().getLocation())
                .setSystemId(getMinionIdentity().getId());
        return builder.build();
    }

    private TwinResponseBean mapTwinResponseToProto(byte[] responseBytes) {
        TwinResponseBean twinResponseBean = new TwinResponseBean();
        try {
            TwinResponseProto twinResponseProto = TwinResponseProto.parseFrom(responseBytes);

            if (!Strings.isNullOrEmpty(twinResponseProto.getLocation())) {
                twinResponseBean.setLocation(twinResponseProto.getLocation());
            }
            twinResponseBean.setKey(twinResponseProto.getConsumerKey());
            if (twinResponseProto.getTwinObject() != null) {
                twinResponseBean.setObject(twinResponseProto.getTwinObject().toByteArray());
            }
            return twinResponseBean;
        } catch (InvalidProtocolBufferException e) {
            LOG.error("Failed to parse response from proto", e);
        }
        return twinResponseBean;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        byte[] sinkUpdateBytes = exchange.getIn().getBody(byte[].class);
        TwinResponseBean twinResponseBean = mapTwinResponseToProto(sinkUpdateBytes);
        accept(twinResponseBean);
    }

    private class SinkRouteBuilder extends RouteBuilder {

        private final Processor processor;

        private SinkRouteBuilder(Processor processor) {
            super(sinkCamelContext);
            this.processor = processor;
        }

        @Override
        public void configure() throws Exception {
            String queueName = String.format(TWIN_QUEUE_NAME_FORMAT, SystemInfoUtils.getInstanceId(), "Twin.Sink");
            final JmsEndpoint endpoint = getContext().getEndpoint(String.format("queuingservice:%s",
                    queueName), JmsEndpoint.class);
            from(endpoint).setExchangePattern(ExchangePattern.InOnly)
                    .process(processor)
                    .routeId(SystemInfoUtils.getInstanceId() + ".Twin.Sink");
        }
    }
}
