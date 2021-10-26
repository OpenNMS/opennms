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

package org.opennms.features.config.dao.impl.util;

<<<<<<< HEAD
import org.eclipse.persistence.internal.oxm.ByteArraySource;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
=======
import static org.opennms.features.config.dao.impl.util.TopLevelElementToClass.topLevelElementToClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.internal.oxm.ByteArraySource;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
import org.eclipse.persistence.oxm.MediaType;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.XmlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
<<<<<<< HEAD
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class XmlMapper<CONFIG_CLASS> {
    private static final Logger LOG = LoggerFactory.getLogger(XmlMapper.class);

    private final XmlSchema xmlSchema;
    private final Class<CONFIG_CLASS> configurationClass;
    private final JAXBContext jc;
    private final Unmarshaller unmarshaller;
    private final Marshaller marshaller;

    public XmlMapper(XmlSchema xmlSchema, Class<CONFIG_CLASS> configurationClass)
            throws NullPointerException, JAXBException {
        Objects.requireNonNull(xmlSchema);
        Objects.requireNonNull(configurationClass);
        this.xmlSchema = xmlSchema;
        this.configurationClass = configurationClass;
        jc = JaxbUtils.getContextFor(configurationClass);
        unmarshaller = jc.createUnmarshaller();
        marshaller = jc.createMarshaller();
    }

    public boolean validate(CONFIG_CLASS obj) throws RuntimeException {
        if (xmlSchema == null || xmlSchema.getXsdContent() == null) {
=======
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import com.google.common.base.Strings;

public class XmlMapper {
    private static final Logger LOG = LoggerFactory.getLogger(XmlMapper.class);

    private final XmlSchema xmlSchema;
    private final DynamicJAXBContext jaxbContext;

    public XmlMapper(XmlSchema xmlSchema)
            throws NullPointerException {
        this.xmlSchema = Objects.requireNonNull(xmlSchema);
        this.jaxbContext = getDynamicJAXBContextForService(xmlSchema);
    }

    public boolean validate(String configAsXml) throws RuntimeException {
        if (xmlSchema.getXsdContent() == null) {
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
            return false;
        }
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = factory.newSchema(new StreamSource(new StringReader(this.xmlSchema.getXsdContent())));
            if (schema == null) {
                return false;
            }
<<<<<<< HEAD
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setSchema(schema);
            marshaller.marshal(obj, new DefaultHandler());
            return true;
        } catch (Exception e) {
            LOG.warn("an error occurred while attempting to load schema validation files for class {}",
                    this.configurationClass, e);
=======
            schema.newValidator().validate(new StreamSource(new StringReader(configAsXml)));
            return true;
        } catch (Exception e) {
            LOG.warn("an error occurred while attempting to load schema validation files for {}.",
                    xmlSchema.getTopLevelObject(), e);
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
            throw new RuntimeException(e.getCause());
        }
    }

    public String jsonToXml(String json) {
<<<<<<< HEAD
        CONFIG_CLASS obj = this.jsonToJaxbObject(json);
        return this.jaxbObjectToXml(obj);
    }

    public String xmlToJson(String sourceXml) {
        CONFIG_CLASS obj = this.xmlToJaxbObject(sourceXml);
        return this.jaxbObjectToJson(obj);
    }

    public CONFIG_CLASS jsonToJaxbObject(String json) {
        try {
            unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
            unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);
            ByteArraySource source = new ByteArraySource(json.getBytes(StandardCharsets.UTF_8));
            JAXBElement<CONFIG_CLASS> jaxbElement = unmarshaller.unmarshal(source, configurationClass);
            return jaxbElement.getValue();
=======
        try {
            final Unmarshaller u = jaxbContext.createUnmarshaller();
            u.setProperty(UnmarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
            u.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, false);

            Class<? extends DynamicEntity> entityClass = getTopLevelEntity(jaxbContext);
            ByteArraySource byteArraySource = new ByteArraySource(json.getBytes(StandardCharsets.UTF_8));
            DynamicEntity entity = u.unmarshal(byteArraySource, entityClass).getValue();

            final Marshaller m = jaxbContext.createMarshaller();
            final StringWriter writer = new StringWriter();
            m.marshal(entity, writer);
            return writer.toString();
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

<<<<<<< HEAD
    /**
     * Convert config object to json (it will not validate)
     *
     * @param config object matching the Generic type
     * @return xml string
     */
    public String jaxbObjectToJson(CONFIG_CLASS config) {
        try {
            marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
            marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);
            final StringWriter writer = new StringWriter();
            marshaller.marshal(config, writer);
            return writer.toString();
        } catch (JAXBException e) {
=======
    public String xmlToJson(String sourceXml) {
        try {
            final XMLFilter filter = JaxbUtils.getXMLFilterForNamespace(this.xmlSchema.getNamespace());
            final InputSource inputSource = new InputSource(new StringReader(sourceXml));
            final SAXSource source = new SAXSource(filter, inputSource);

            final Unmarshaller u = jaxbContext.createUnmarshaller();
            DynamicEntity entity = (DynamicEntity) u.unmarshal(source);

            final Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
            m.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, false);

            final StringWriter writer = new StringWriter();
            m.marshal(entity, writer);
            return writer.toString();
        } catch (JAXBException | SAXException e) {
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
            throw new RuntimeException(e);
        }
    }

<<<<<<< HEAD
    /**
     * Convert config object to xml (it will not validate)
     *
     * @param config object matching the Generic type
     * @return xml string
     */
    public String jaxbObjectToXml(CONFIG_CLASS config) {
        try {
            marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, MediaType.APPLICATION_XML);
            final StringWriter writer = new StringWriter();
            marshaller.marshal(config, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert xml to config object (it will not validate)
     *
     * @param sourceXml xml in string format
     * @return config object
     */
    public CONFIG_CLASS xmlToJaxbObject(String sourceXml) {
        final XMLFilter filter;
        try {
            filter = JaxbUtils.getXMLFilterForClass(configurationClass);
            final InputSource inputSource = new InputSource(new StringReader(sourceXml));
            final SAXSource source = new SAXSource(filter, inputSource);

            final Unmarshaller u = jc.createUnmarshaller();
            return u.unmarshal(source, configurationClass).getValue();
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }
}
=======
    private Class<? extends DynamicEntity> getTopLevelEntity(DynamicJAXBContext jc) {
        String className= namespace2package(xmlSchema.getNamespace()) +
                "." +
                topLevelElementToClass(xmlSchema.getTopLevelObject());
        return jc.newDynamicEntity(className).getClass();
    }

    public static String namespace2package(String s) {
        // "http://xmlns.opennms.org/xsd/config/vacuumd" -> "org.opennms.xmlns.xsd.config.vacuumd"
        final URL url;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        StringBuilder pkgName = new StringBuilder();

        // Split and reverse the host part
        String[] parts = url.getHost().split("\\.");
        for (int i = parts.length - 1; i>=0; i--) {
            if (i != parts.length - 1) {
                pkgName.append(".");
            }
            pkgName.append(parts[i]);
        }

        // Split and append the parts of the path
        parts = url.getPath().split("/");
        for (String part : parts) {
            if (Strings.isNullOrEmpty(part)) {
                continue;
            }
            pkgName.append(".");
            pkgName.append(part);
        }

        String packageName = pkgName.toString();
        packageName = packageName.replace('-', '_');
        return packageName;
    }

    private DynamicJAXBContext getDynamicJAXBContextForService(XmlSchema xmlSchema) {
        final String xsd = xmlSchema.getXsdContent();

        try (InputStream is = new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8))) {
            return DynamicJAXBContextFactory.createContextFromXSD(is, null, null, null);
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
>>>>>>> aad402a2f24c28b6220717cc8e172825bd940a63
