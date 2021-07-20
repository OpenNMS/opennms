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

public class MockTwinSubscriber implements TwinSubscriber {

    private final TwinSubscriberBroker twinSubscriberBroker;

    private final Map<String, Class<?>> classesByKey = new ConcurrentHashMap<>();
    private final Map<String, Consumer<?>> consumerMap = new ConcurrentHashMap<>();
    private final Map<Closeable, String> closeableStringMap = new ConcurrentHashMap<>();

    public MockTwinSubscriber(TwinSubscriberBroker twinSubscriberBroker) {
        this.twinSubscriberBroker = twinSubscriberBroker;
    }


    @Override
    public <T> Closeable getObject(String key, Class<T> clazz, Consumer<T> consumer) {
        classesByKey.put(key, clazz);
        consumerMap.put(key, consumer);
        Closeable closeable = new Closeable() {
            @Override
            public void close() throws IOException {
                 String key = closeableStringMap.get(this);
                 consumerMap.remove(key);
                 classesByKey.remove(key);
            }
        };
        closeableStringMap.put(closeable, key);
        twinSubscriberBroker.getObject(key, new Consumer<TwinResponse>() {
            @Override
            public void accept(TwinResponse twinResponse) {
                Consumer<?> consumer = consumerMap.get(twinResponse.getKey());
                // Unmarshal and call consumer accept.
            }
        });
        return closeable;
    }
}
