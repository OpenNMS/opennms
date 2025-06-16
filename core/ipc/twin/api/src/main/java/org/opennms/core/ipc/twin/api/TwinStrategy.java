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
package org.opennms.core.ipc.twin.api;

import com.google.common.base.Strings;

public class TwinStrategy {

    public static final String IPC_STRATEGY = "org.opennms.core.ipc.strategy";

    public static final String TWIN_STRATEGY_PROPERTY = "org.opennms.core.ipc.twin.strategy";

    public static final String LOG_PREFIX = "ipc";

    private static final String JMS_TWIN_STRATEGY_NAME = "jms";

    private static final String KAFKA_TWIN_STRATEGY_NAME = "kafka";

    private static final String GRPC_TWIN_STRATEGY_NAME = "grpc";

    private static final String OSGI_TWIN_STRATEGY_NAME = "osgi";

    public static enum Strategy {
        JMS(JMS_TWIN_STRATEGY_NAME, "JMS implementation using Camel"),
        KAFKA(KAFKA_TWIN_STRATEGY_NAME, "Kafka implementation using the Kafka consumer/producer APIs"),
        GRPC(GRPC_TWIN_STRATEGY_NAME, "GRPC implementation using gRPC APIs"),
        OSGI(OSGI_TWIN_STRATEGY_NAME, "OSGI Delegate implementation");

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

    public static Strategy getTwinStrategy() {
        String effectiveStrategyName = System.getProperty(IPC_STRATEGY);
        if (Strings.isNullOrEmpty(effectiveStrategyName)) {
            effectiveStrategyName = System.getProperty(TWIN_STRATEGY_PROPERTY, JMS_TWIN_STRATEGY_NAME);
        }
        for (Strategy strategy : Strategy.values()) {
            if (strategy.getName().equalsIgnoreCase(effectiveStrategyName)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unsupported Twin strategy: " + effectiveStrategyName);
    }
}
