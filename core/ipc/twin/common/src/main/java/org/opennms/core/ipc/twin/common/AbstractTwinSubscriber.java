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

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.distributed.core.api.MinionIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractTwinSubscriber implements TwinSubscriber {

    private final Map<String, SessionImpl<?>> sessionMap = new ConcurrentHashMap<>();
    private final Map<String, byte[]> objMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MinionIdentity minionIdentity;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTwinSubscriber.class);

    protected AbstractTwinSubscriber(MinionIdentity minionIdentity) {
        this.minionIdentity = minionIdentity;
    }

    /**
     * @param twinRequest Send RpcRequest from @{@link AbstractTwinSubscriber}
     */
    abstract void sendRpcRequest(TwinRequestBean twinRequest);


    @Override
    public <T> Closeable subscribe(String key, Class<T> clazz, Consumer<T> consumer) {
        SessionImpl<T> session = new SessionImpl<T>(key, clazz, consumer);
        sessionMap.put(key, session);
        TwinRequestBean twinRequestBean = new TwinRequestBean(key, minionIdentity.getLocation());
        sendRpcRequest(twinRequestBean);
        LOG.info("Subscribed to object updates with key {}", key);
        return session;
    }

    protected void accept(TwinResponseBean twinResponse) throws IOException {
        LOG.info("Received object update with key {}", twinResponse.getKey());
        SessionImpl<?> session = sessionMap.get(twinResponse.getKey());
        if (session == null) {
            LOG.warn("Session with key {} doesn't exist yet", twinResponse.getKey());
            sessionMap.keySet().forEach(LOG::info);
        }
        if (session != null) {
            session.accept(twinResponse);
        }
    }

    private class SessionImpl<T> implements Closeable {

        private final String key;
        private final Consumer<T> consumer;
        private final Class<T> clazz;

        public SessionImpl(String key, Class<T> clazz, Consumer<T> consumer) {
            this.key = key;
            this.clazz = clazz;
            this.consumer = consumer;
        }

        @Override
        public void close() throws IOException {
            LOG.info("Closed session with key {} ", key);
            sessionMap.remove(key);
        }

        public void accept(TwinResponseBean twinResponseBean) throws IOException {
            if (twinResponseBean.getObject() != null && isObjectUpdated(twinResponseBean.getObject())) {
                objMap.put(twinResponseBean.getKey(), twinResponseBean.getObject());
                final T value = objectMapper.readValue(twinResponseBean.getObject(), clazz);
                LOG.debug("Update consumer with key {}", key);
                consumer.accept(value);
            }
        }

        boolean isObjectUpdated(byte[] updatedObject) {
            byte[] objInBytes = objMap.get(key);
            if (objInBytes == null) {
                return true;
            }
            return !Arrays.equals(objInBytes, updatedObject);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SessionImpl<?> session = (SessionImpl<?>) o;
            return Objects.equals(key, session.key) && Objects.equals(consumer, session.consumer) && Objects.equals(clazz, session.clazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, consumer, clazz);
        }
    }
}
