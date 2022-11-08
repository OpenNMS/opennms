/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.rpc.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.opennms.core.ipc.common.kafka.KafkaConfigProvider;
import org.opennms.core.ipc.common.kafka.Utils;
import org.opennms.core.ipc.rpc.kafka.model.RpcMessageProto;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.MinionIdentity;

import com.codahale.metrics.MetricRegistry;

/**
 * This overrides @{@link org.opennms.core.ipc.rpc.kafka.KafkaRpcServerManager.KafkaConsumerRunner#sendMessageToKafka(RpcMessageProto, String, String)}
 * to send duplicate message or skip a chunk in between.
 */
public class RpcTestServer extends KafkaRpcServerManager {

    private MinionIdentity minionIdentity;

    private boolean skipChunks = false;

    private boolean skippedOrDuplicated = false;

    private KafkaServerConsumer kafkaConsumerRunner;

    public RpcTestServer(KafkaConfigProvider configProvider, MinionIdentity minionIdentity, TracerRegistry tracerRegistry) {
        super(configProvider, minionIdentity, tracerRegistry, new MetricRegistry());
        this.minionIdentity = minionIdentity;
    }

    class KafkaServerConsumer extends KafkaRpcServerManager.KafkaConsumerRunner {

        private KafkaServerConsumer(KafkaConsumer<String, byte[]> consumer, String topic) {
            super(consumer, topic);
        }

        @Override
        void sendMessageToKafka(RpcMessageProto rpcMessage, String topic, String responseAsString) {
            if (skipChunks && rpcMessage.getCurrentChunkNumber() == 2) {
                skipChunks = true;
            }

            if (!skipChunks) {
                super.sendMessageToKafka(rpcMessage, topic, responseAsString);
                skippedOrDuplicated = true;
            }
            if (rpcMessage.getCurrentChunkNumber() == 1) {
                super.sendMessageToKafka(rpcMessage, topic, responseAsString);
                skippedOrDuplicated = true;
            }
        }

    }

    @Override
    protected void startConsumerForModule(RpcModule<RpcRequest, RpcResponse> rpcModule) {
        String topic = getKafkaRpcTopicProvider().getRequestTopicAtLocation(minionIdentity.getLocation(), rpcModule.getId());
        KafkaConsumer<String, byte[]> consumer = Utils.runWithGivenClassLoader(() -> new KafkaConsumer<>(getKafkaConfig()), KafkaConsumer.class.getClassLoader());
        kafkaConsumerRunner = new KafkaServerConsumer(consumer, topic);
        getExecutor().execute(kafkaConsumerRunner);
    }

    @Override
    protected void stopConsumerForModule(RpcModule<RpcRequest, RpcResponse> rpcModule) {
      if(kafkaConsumerRunner != null) {
          kafkaConsumerRunner.shutdown();
      }
    }


    @Override
    public void destroy() {
        kafkaConsumerRunner.shutdown();
        super.destroy();
    }

    public void setSkipChunks(boolean skipChunks) {
        this.skipChunks = skipChunks;
    }

    public boolean isSkippedOrDuplicated() {
        return skippedOrDuplicated;
    }
}
