/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.common.kafka;

import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.RPC_REQUEST_TOPIC_NAME;
import static org.opennms.core.ipc.common.kafka.KafkaRpcConstants.RPC_RESPONSE_TOPIC_NAME;
import org.opennms.core.utils.SystemInfoUtils;

public class KafkaTopicProvider {

    private static final String TOPIC_NAME_AT_LOCATION = "%s.%s.%s";
    private static final String TOPIC_NAME_WITHOUT_LOCATION = "%s.%s";
    private static final String TOPIC_NAME_WITH_MODULE = "%s.%s.%s";
    private static final String TOPIC_NAME_WITH_MODULE_AND_LOCATION = "%s.%s.%s.%s";
    private final boolean singleTopic;

    public KafkaTopicProvider(boolean singleTopic) {
        this.singleTopic = singleTopic;
    }

    public KafkaTopicProvider() {
        this.singleTopic = true;
    }

    public String getRequestTopicAtLocation(String location, String module) {
        if (singleTopic) {
            return String.format(TOPIC_NAME_AT_LOCATION, SystemInfoUtils.getInstanceId(), location, RPC_REQUEST_TOPIC_NAME);
        }
        return String.format(TOPIC_NAME_WITH_MODULE_AND_LOCATION, SystemInfoUtils.getInstanceId(), location, RPC_REQUEST_TOPIC_NAME, module);
    }

    public String getResponseTopic(String module) {
        if (singleTopic) {
            return String.format(TOPIC_NAME_WITHOUT_LOCATION, SystemInfoUtils.getInstanceId(), RPC_RESPONSE_TOPIC_NAME);
        }
        return String.format(TOPIC_NAME_WITH_MODULE, SystemInfoUtils.getInstanceId(), RPC_RESPONSE_TOPIC_NAME, module);
    }

}
