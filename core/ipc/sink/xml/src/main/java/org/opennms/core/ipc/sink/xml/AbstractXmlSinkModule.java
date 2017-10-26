/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.xml;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.xml.XmlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.ipc.sink.api.Message;

public abstract class AbstractXmlSinkModule<S extends Message, T extends Message> implements SinkModule<S, T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractXmlSinkModule.class);

    private final Class<T> messageClazz;

    /**
     * Store a thread-local reference to the {@link XmlHandler} because 
     * Unmarshalers are not thread-safe.
     */
    private final ThreadLocal<XmlHandler<T>> messageXmlHandler = new ThreadLocal<>();

    public AbstractXmlSinkModule(Class<T> messageClazz) {
        this.messageClazz = Objects.requireNonNull(messageClazz);
    }

    @Override
    public byte[] marshal(T message) {
        return getXmlHandler().marshal(message).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public T unmarshal(byte[] bytes) {
        return getXmlHandler().unmarshal(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        return getClass() == obj.getClass();
    }

    private XmlHandler<T> getXmlHandler() {
        XmlHandler<T> xmlHandler = messageXmlHandler.get();
        if (xmlHandler == null) {
            xmlHandler = createXmlHandler(messageClazz);
            messageXmlHandler.set(xmlHandler);
        }
        return xmlHandler;
    }

    private <W> XmlHandler<W> createXmlHandler(Class<W> clazz) {
        try {
            return new XmlHandler<>(clazz);
        } catch (Throwable t) {
            // NMS-8793: This is a work-around for some failure in the Minion container
            // When invoked for the first time, the creation may fail due to
            // errors of the form "invalid protocol handler: mvn", but subsequent
            // calls always seem to work
            LOG.warn("Creating the XmlHandler failed. Retrying.", t);
            return new XmlHandler<>(clazz);
        }
    }
}
