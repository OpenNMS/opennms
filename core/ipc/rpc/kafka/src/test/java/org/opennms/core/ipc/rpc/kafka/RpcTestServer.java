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
