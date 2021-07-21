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

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MockTwinSubscriber implements TwinSubscriber {

    private final TwinSubscriberBroker twinSubscriberBroker;

    private final Map<String, Class<?>> classesByKey = new ConcurrentHashMap<>();
    private final Map<String, Consumer<?>> consumerMap = new ConcurrentHashMap<>();
    private final Map<Closeable, String> closableMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MockTwinSubscriber(TwinSubscriberBroker twinSubscriberBroker) {
        this.twinSubscriberBroker = twinSubscriberBroker;
        twinSubscriberBroker.registerProvider(new Consumer<TwinResponse>() {
            @Override
            public void accept(TwinResponse twinResponse) {
                String key = twinResponse.getKey();
                if (key != null) {
                    Consumer<?> consumer = consumerMap.get(key);
                    if(consumer != null) {
                        consumer.accept(unmarshalResponse(key, twinResponse.getObject()));
                    }
                }
            }
        });
    }

    private <T> T unmarshalResponse(String key, byte[] value) {
        Class<?> clazz = classesByKey.get(key);
        try {
            return (T) objectMapper.readValue(value, clazz);
        } catch (IOException e) {
            // Ignore
        }
        return null;
    }


    @Override
    public <T> Closeable getObject(String key, Class<T> clazz, Consumer<T> consumer) {
        classesByKey.put(key, clazz);
        consumerMap.put(key, consumer);
        Closeable closeable = new Closeable() {
            @Override
            public void close() throws IOException {
                String key = closableMap.get(this);
                consumerMap.remove(key);
                classesByKey.remove(key);
            }
        };
        closableMap.put(closeable, key);
        TwinRequest twinRequest = new MockTwinRequest(key, "MINION");
        twinSubscriberBroker.sendRequest(twinRequest);
        return closeable;
    }
}
