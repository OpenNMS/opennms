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
