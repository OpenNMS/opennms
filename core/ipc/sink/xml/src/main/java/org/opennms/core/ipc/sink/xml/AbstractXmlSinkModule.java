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

import java.util.Objects;

import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.xml.XmlHandler;
import org.opennms.core.ipc.sink.api.Message;

public abstract class AbstractXmlSinkModule<T extends Message> implements SinkModule<T> {

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
    public String marshal(T message) {
        return getXmlHandler().marshal(message);
    }

    @Override
    public T unmarshal(String message) {
        return getXmlHandler().unmarshal(message);
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
            // This is a work-around for some failure in the Minion container
            // When invoked for the first time, the creation may fail due to
            // errors of the form "invalid protocol handler: mvn", but subsequent
            // calls always seem to work
            return new XmlHandler<>(clazz);
        }
    }
}
