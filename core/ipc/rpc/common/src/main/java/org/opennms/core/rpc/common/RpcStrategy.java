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

package org.opennms.core.rpc.common;

public class RpcStrategy {

    public static final String RPC_STRATEGY_PROPERTY = "org.opennms.core.ipc.rpc.strategy";

    private static final String JMS_RPC_STRATEGY_NAME = "jms";

    private static final String SQS_RPC_STRATEGY_NAME = "sqs";

    public static enum Strategy {
        JMS(JMS_RPC_STRATEGY_NAME, "JMS implementation using Camel"),
        SQS(SQS_RPC_STRATEGY_NAME, "Amazon SQS implementation");

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
        final String effectiveStrategyName = System.getProperty(RPC_STRATEGY_PROPERTY, JMS_RPC_STRATEGY_NAME);
        for (Strategy strategy : Strategy.values()) {
            if (strategy.getName().equalsIgnoreCase(effectiveStrategyName)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unsupported RPC strategy: " + effectiveStrategyName);
    }
}
