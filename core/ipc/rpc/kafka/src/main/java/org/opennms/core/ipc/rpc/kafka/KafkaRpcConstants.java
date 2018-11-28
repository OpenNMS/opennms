/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import java.util.Properties;

public interface KafkaRpcConstants {
    
    public static final String KAFKA_CONFIG_PID = "org.opennms.core.ipc.rpc.kafka";
    public static final String KAFKA_CONFIG_SYS_PROP_PREFIX = KAFKA_CONFIG_PID + ".";
    public static final String RPC_REQUEST_TOPIC_NAME = "rpc-request";
    public static final String RPC_RESPONSE_TOPIC_NAME = "rpc-response";
    //By default, kafka allows 1MB buffer sizes, here rpcContent (refer to proto/rpc.proto) is limited to 900KB to allow space for other parameters in proto file.
    public static final int MAX_BUFFER_SIZE_CONFIGURED = 921600;
    public static final String MAX_BUFFER_SIZE_PROPERTY = "max.buffer.size";

    static int getTotalChunks(int messageSize, int maxBufferSize) {
        int totalChunks = 0;
        if (messageSize > maxBufferSize) {
            totalChunks = (messageSize / maxBufferSize) + (messageSize % maxBufferSize == 0 ? 0 : 1);
        } else {
            totalChunks = 1;
        }
        return totalChunks;
    };

    static int getBufferSize(int messageSize, int maxBufferSize, int chunk) {
        int bufferSize = messageSize;
        if (getTotalChunks(messageSize, maxBufferSize) > 1) {
            bufferSize = (messageSize - chunk * maxBufferSize > maxBufferSize) ? maxBufferSize : messageSize - chunk * maxBufferSize;
        }
        return bufferSize;
    }

    static int getMaxBufferSize() {
        //Configurable buffer size but it should be always less than MAX_BUFFER_SIZE_CONFIGURED.
        int maxBufferSize = Math.min(MAX_BUFFER_SIZE_CONFIGURED,
                Integer.getInteger(String.format("%s%s", KAFKA_CONFIG_SYS_PROP_PREFIX, MAX_BUFFER_SIZE_PROPERTY), MAX_BUFFER_SIZE_CONFIGURED));
        return maxBufferSize;
    }

    static int getMaxBufferSize(Properties properties) {
        int maxBufferSize = MAX_BUFFER_SIZE_CONFIGURED;
        try {
            maxBufferSize = Math.min(MAX_BUFFER_SIZE_CONFIGURED, Integer.parseInt(properties.getProperty(MAX_BUFFER_SIZE_PROPERTY)));
        } catch (NumberFormatException e) {
            // pass
        }
        return maxBufferSize;
    }

}
