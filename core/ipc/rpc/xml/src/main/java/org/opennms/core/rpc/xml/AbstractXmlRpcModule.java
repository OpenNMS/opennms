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
package org.opennms.core.rpc.xml;

import java.util.Objects;

import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.xml.XmlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RpcModule} that uses JaxbUtils for marshaling and unmarshaling requests.
 *
 * @author jwhite
 */
public abstract class AbstractXmlRpcModule<S extends RpcRequest,T extends RpcResponse> implements RpcModule<S, T>  {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractXmlRpcModule.class);

    private final Class<S> requestClazz;
    private final Class<T> responseClazz;

    /**
     * Store a thread-local reference to the {@link XmlHandler} because 
     * Unmarshalers are not thread-safe.
     */
    private final ThreadLocal<XmlHandler<S>> requestXmlHandler = new ThreadLocal<>();
    private final ThreadLocal<XmlHandler<T>> responseXmlHandler = new ThreadLocal<>();

    public AbstractXmlRpcModule(Class<S> requestClazz, Class<T> responseClazz) {
        this.requestClazz = Objects.requireNonNull(requestClazz);
        this.responseClazz = Objects.requireNonNull(responseClazz);
    }

    @Override
    public String marshalRequest(S request) {
        return getRequestXmlHandler().marshal(request);
    }

    @Override
    public S unmarshalRequest(String requestXml) {
        return getRequestXmlHandler().unmarshal(requestXml);
    }

    @Override
    public String marshalResponse(T response) {
        return getResponseXmlHandler().marshal(response);
    }

    @Override
    public T unmarshalResponse(String response) {
        return getResponseXmlHandler().unmarshal(response);
    }

    private XmlHandler<S> getRequestXmlHandler() {
        XmlHandler<S> xmlHandler = requestXmlHandler.get();
        if (xmlHandler == null) {
            xmlHandler = createXmlHandler(requestClazz);
            requestXmlHandler.set(xmlHandler);
        }
        return xmlHandler;
    }

    private XmlHandler<T> getResponseXmlHandler() {
        XmlHandler<T> xmlHandler = responseXmlHandler.get();
        if (xmlHandler == null) {
            xmlHandler = createXmlHandler(responseClazz);
            responseXmlHandler.set(xmlHandler);
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
