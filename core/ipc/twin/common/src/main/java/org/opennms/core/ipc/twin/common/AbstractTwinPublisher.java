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

package org.opennms.core.ipc.twin.common;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.ipc.twin.api.TwinPublisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractTwinPublisher implements TwinPublisher {

    private final Map<String, byte[]> objMap = new ConcurrentHashMap<>();
    private final Map<Session<?>, String> keySessionMap = new ConcurrentHashMap<>();
    protected final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param sinkUpdate Handle sink Update from @{@link AbstractTwinPublisher}.
     */
    abstract void handleSinkUpdate(TwinResponseBean sinkUpdate);

    @Override
    public <T> Session<T> register(T obj, String key, String location) throws IOException {
        // Marshal obj to byte array.
        byte[] value = marshalObject(obj);
        if (location == null) {
            location = "";
        }
        objMap.put(key + location, value);
        Session<T> session = new SessionImpl<T>(key);
        // TODO: Better way to mapping to location.
        keySessionMap.put(session, key + location);
        return session;
    }


    protected TwinResponseBean getTwin(TwinRequestBean twinRequest) {
        byte[] value = objMap.get(twinRequest.getKey() + twinRequest.getLocation());
        if (value == null) {
            value = objMap.get(twinRequest.getKey());
        }
        // TODO: What if there is no value exist here ?
        return new TwinResponseBean(twinRequest.getKey(), twinRequest.getLocation(), value);
    }

    private <T> byte[] marshalObject(T obj) throws IOException {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw e;
        }
    }

    private class SessionImpl<T> implements Session<T> {

        private final String key;

        public SessionImpl(String key) {
            this.key = key;
        }

        @Override
        public void publish(T obj, String location) throws IOException {
            try {
                byte[] value = objectMapper.writeValueAsBytes(obj);
                if (location == null) {
                    location = "";
                }
                objMap.put(key + location, value);
                handleSinkUpdate(new TwinResponseBean(key, location, value));
            } catch (JsonProcessingException e) {
                throw e;
            }
        }

        @Override
        public void close() throws IOException {
            String key = keySessionMap.remove(this);
            if (key != null) {
                objMap.remove(key);
            }
        }
    }

}
