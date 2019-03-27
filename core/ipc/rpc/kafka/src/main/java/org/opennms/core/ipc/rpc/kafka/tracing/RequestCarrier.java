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

package org.opennms.core.ipc.rpc.kafka.tracing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.core.ipc.rpc.kafka.model.RpcMessageProtos;

import io.opentracing.propagation.TextMap;

public class RequestCarrier implements TextMap {

    private final RpcMessageProtos.RpcMessage.Builder builder;

    public RequestCarrier(RpcMessageProtos.RpcMessage.Builder builder) {
        this.builder = builder;
    }

    @Override
    public void put(String key, String value) {
        RpcMessageProtos.TracingInfo tracingInfo = RpcMessageProtos.TracingInfo.newBuilder()
                .setKey(key)
                .setValue(value).build();
        builder.addTracingInfo(tracingInfo);
    }

    public static Map<String,String> getTracingInfoMap(List<RpcMessageProtos.TracingInfo> tracingInfoList) {
        Map<String, String> tracingInfoMap = new HashMap<>();
        tracingInfoList.forEach(tracingInfo -> {
            tracingInfoMap.put(tracingInfo.getKey(), tracingInfo.getValue());
        });
        return tracingInfoMap;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException();
    }
}
