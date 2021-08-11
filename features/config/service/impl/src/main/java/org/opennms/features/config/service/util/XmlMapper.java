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

package org.opennms.features.config.service.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.internal.oxm.ByteArraySource;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.eclipse.persistence.oxm.MediaType;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.XMLSchema;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import com.google.common.base.Strings;


public class XmlMapper<T> {
    private XMLSchema xmlSchema;
    private Class<T> configurationClass;

    public XmlMapper(XMLSchema xmlSchema, Class<T> configurationClass) throws NullPointerException {
        Objects.requireNonNull(xmlSchema);
        Objects.requireNonNull(configurationClass);
        this.xmlSchema = xmlSchema;
        this.configurationClass = configurationClass;
    }

    public String jsonToXml(String json) {
        T obj = this.jsonToJaxbObject(json);
        return JaxbUtils.marshal(obj);
    }

    private DynamicJAXBContext getDynamicJAXBContextForService() {
        final String xsd = xmlSchema.getXsdContent();
        if (xsd == null) {
            throw new IllegalArgumentException("No XSD found for service: " + xmlSchema.getClass() + ". Cannot perform XML related operations.");
        }

        try (InputStream is = new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8))) {
            return DynamicJAXBContextFactory.createContextFromXSD(is, null, null, null);
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String xmlToJson(String sourceXml) {
        try {
            DynamicJAXBContext jc = getDynamicJAXBContextForService();
            final XMLFilter filter = JaxbUtils.getXMLFilterForClass(configurationClass);
            final InputSource inputSource = new InputSource(new StringReader(sourceXml));
            final SAXSource source = new SAXSource(filter, inputSource);

            final Unmarshaller u = jc.createUnmarshaller();
            DynamicEntity entity = (DynamicEntity) u.unmarshal(source);

            final Marshaller m = jc.createMarshaller();
            m.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
            m.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);

            final StringWriter writer = new StringWriter();
            m.marshal(entity, writer);
            return writer.toString();
        } catch (JAXBException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    public T jsonToJaxbObject(String json) {
        try {
            final JAXBContext jc = JaxbUtils.getContextFor(configurationClass);
            final Unmarshaller u = jc.createUnmarshaller();
            u.setProperty(UnmarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
            u.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);

            ByteArraySource source = new ByteArraySource(json.getBytes(StandardCharsets.UTF_8));
            JAXBElement<T> jaxbElement = u.unmarshal(source, configurationClass);
            return jaxbElement.getValue();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public String jaxbObjectToJson(Object obj) {
        try {
            final JAXBContext jc = JaxbUtils.getContextFor(obj.getClass());
            final Marshaller m = jc.createMarshaller();
            m.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
            m.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);

            final StringWriter writer = new StringWriter();
            m.marshal(obj, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public T xmlToJaxbObject(String sourceXml) {
        return JaxbUtils.unmarshal(configurationClass, sourceXml);
    }
}
