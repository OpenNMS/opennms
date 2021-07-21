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

package org.opennms.core.ipc.twin.api;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MockTwinPublisher implements TwinPublisher {

    private final TwinPublisherBroker twinPublisherBroker;
    private final TwinPublisherBroker.SinkUpdate sinkUpdate;
    private final Map<String, byte[]> objMap = new ConcurrentHashMap<>();
    private final Map<Session<?>, String> keySessionMap = new ConcurrentHashMap<>();

    public MockTwinPublisher(TwinPublisherBroker twinPublisherBroker) {
        this.twinPublisherBroker = twinPublisherBroker;
        sinkUpdate = twinPublisherBroker.register(this::apply);
    }

    @Override
    public <T> Session<T> register(T obj, String key) {
        // Marshal obj to byte array.
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            byte[] value = objectMapper.writeValueAsBytes(obj);
            objMap.put(key, value);
        } catch (JsonProcessingException e) {
            //Ignore
        }
        Session<T> session = new Session<T>() {
            @Override
            public void publish(Object obj) {
                try {
                    byte[] value = objectMapper.writeValueAsBytes(obj);
                    objMap.put(key, value);
                    sinkUpdate.update(new MockTwinResponse(key, value));
                } catch (JsonProcessingException e) {
                    // Ignore
                }
            }

            @Override
            public void close() throws IOException {
                String key = keySessionMap.remove(this);
                objMap.remove(key);
            }
        };
        keySessionMap.put(session, key);
        return session;

    }

    TwinResponse apply(TwinRequest twinRequest) {
        String key = twinRequest.getKey();
        byte[] value = objMap.get(key);
        return new MockTwinResponse(key, value);
    }


}
