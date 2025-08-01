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
package org.opennms.core.rpc.common;

import com.google.common.base.Strings;

public class RpcStrategy {

    public static final String IPC_STRATEGY = "org.opennms.core.ipc.strategy";

    public static final String RPC_STRATEGY_PROPERTY = "org.opennms.core.ipc.rpc.strategy";

    private static final String JMS_RPC_STRATEGY_NAME = "jms";

    private static final String KAFKA_RPC_STRATEGY_NAME = "kafka";

    private static final String GRPC_RPC_STRATEGY_NAME = "grpc";

    private static final String OSGI_RPC_STRATEGY_NAME = "osgi";

    public static enum Strategy {
        JMS(JMS_RPC_STRATEGY_NAME, "JMS implementation using Camel"),
        KAFKA(KAFKA_RPC_STRATEGY_NAME, "Kafka implementation"),
        GRPC(GRPC_RPC_STRATEGY_NAME, "GRPC implementation"),
        OSGI(OSGI_RPC_STRATEGY_NAME, "OSGI Delegate implementation");

        private final String m_name;
        private final String m_descr;

        Strategy(String name, String descr) {
            m_name = name;
            m_descr = descr;
        }
        Strategy(String name) {
            this(name, name);
        }

        public String getName() {
            return m_name;
        }

        public String getDescr() {
            return m_descr;
        }
    }

    public static Strategy getRpcStrategy() {
        String effectiveStrategyName = System.getProperty(IPC_STRATEGY);
        if (Strings.isNullOrEmpty(effectiveStrategyName)) {
            effectiveStrategyName = System.getProperty(RPC_STRATEGY_PROPERTY, JMS_RPC_STRATEGY_NAME);
        }
        for (Strategy strategy : Strategy.values()) {
            if (strategy.getName().equalsIgnoreCase(effectiveStrategyName)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unsupported RPC strategy: " + effectiveStrategyName);
    }
}
