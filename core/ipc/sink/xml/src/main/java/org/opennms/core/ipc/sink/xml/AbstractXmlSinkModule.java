/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
    public byte[] marshalSingleMessage(S message) {
        return marshal((T)getAggregationPolicy().aggregate(null, message));
    }

    /** Modules with different aggregated message should override this method **/
    @Override
    public S unmarshalSingleMessage(byte[] bytes) {
        T log = unmarshal(bytes);
        return (S)log;
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
