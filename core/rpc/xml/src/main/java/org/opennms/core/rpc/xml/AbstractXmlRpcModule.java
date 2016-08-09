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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.IOUtils;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.xml.JaxbUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

/**
 * {@link RpcModule} that uses JaxbUtils for marshaling and unmarshaling requests.
 *
 * @author jwhite
 */
public abstract class AbstractXmlRpcModule<S extends RpcRequest,T extends RpcResponse> implements RpcModule<S, T>  {

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
            // This is a work-around for some failure in the Minion container
            // When invoked for the first time, the creation may fail due to
            // errors of the form "invalid protocol handler: mvn", but subsequent
            // calls always seem to work
            return new XmlHandler<>(clazz);
        }
    }

    private static class XmlHandler<U> {
        private final Class<U> clazz;
        private final Marshaller marshaller;
        private final Unmarshaller unmarshaller;
        private final XMLFilter filter;

        public XmlHandler(Class<U> clazz) {
            this.clazz = clazz;
            JAXBContext context;
            try {
                context = JaxbUtils.getContextFor(clazz);
                filter = JaxbUtils.getXMLFilterForClass(clazz);
            } catch (JAXBException|SAXException e) {
                throw new RuntimeException(e);
            }
            this.marshaller = JaxbUtils.getMarshallerFor(clazz, context);
            this.unmarshaller = JaxbUtils.getUnmarshallerFor(clazz, context, false);
        }

        public String marshal(U obj) {
            final StringWriter jaxbWriter = new StringWriter();
            try {
                marshaller.marshal(obj, jaxbWriter);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            return jaxbWriter.toString();
        }

        public U unmarshal(String xml) {
            final StringReader sr = new StringReader(xml);
            final InputSource is = new InputSource(sr);
            try {
                final SAXSource source = new SAXSource(filter, is);
                final JAXBElement<U> element = unmarshaller.unmarshal(source, clazz);
                return element.getValue();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(sr);
            }
        }
    }
}
