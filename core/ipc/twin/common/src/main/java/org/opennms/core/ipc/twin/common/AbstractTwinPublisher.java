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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractTwinPublisher implements TwinPublisher {

    private final Map<SessionKey, byte[]> objMap = new ConcurrentHashMap<>();
    protected final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTwinPublisher.class);

    /**
     * @param sinkUpdate Handle sink Update from @{@link AbstractTwinPublisher}.
     */
    protected abstract void handleSinkUpdate(TwinResponseBean sinkUpdate);

    @Override
    public <T> Session<T> register(String key, Class<T> clazz, String location) throws IOException {
        SessionKey sessionKey = new SessionKey(key, location);
        LOG.info("Registered a session with key {}", sessionKey);
        return new SessionImpl<>(sessionKey);
    }

    protected TwinResponseBean getTwin(TwinRequestBean twinRequest) {
        byte[] value = objMap.get(new SessionKey(twinRequest.getKey(), twinRequest.getLocation()));
        if (value == null) {
            value = objMap.get(new SessionKey(twinRequest.getKey(), null));
        }
        return new TwinResponseBean(twinRequest.getKey(), twinRequest.getLocation(), value);
    }

    private class SessionImpl<T> implements Session<T> {

        private final SessionKey sessionKey;

        public SessionImpl(SessionKey sessionKey) {
            this.sessionKey = sessionKey;
        }

        @Override
        public void publish(T obj) throws IOException {
            LOG.info("Published an object update for the session with key {}", sessionKey.toString());
            byte[] value = objectMapper.writeValueAsBytes(obj);
            objMap.put(sessionKey, value);
            handleSinkUpdate(new TwinResponseBean(sessionKey.key, sessionKey.location, value));
        }

        @Override
        public void close() throws IOException {
            LOG.info("Closed session with key {} ", sessionKey);
            objMap.remove(sessionKey);
        }
    }

    public static class SessionKey {

        public final String key;
        public final String location;

        private SessionKey(String key, String location) {
            this.key = key;
            this.location = location;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SessionKey that = (SessionKey) o;
            return Objects.equals(key, that.key) && Objects.equals(location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, location);
        }

        @Override
        public String toString() {
            return "SessionKey{" +
                    "key='" + key + '\'' +
                    ", location='" + location + '\'' +
                    '}';
        }
    }

}
