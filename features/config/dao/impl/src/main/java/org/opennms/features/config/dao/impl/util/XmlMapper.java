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
import org.eclipse.persistence.oxm.MediaType;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.XmlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
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
            return false;
        }
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = factory.newSchema(new StreamSource(new StringReader(this.xmlSchema.getXsdContent())));
            if (schema == null) {
                return false;
            }
            schema.newValidator().validate(new StreamSource(new StringReader(configAsXml)));
            return true;
        } catch (Exception e) {
            LOG.warn("an error occurred while attempting to load schema validation files for {}.",
                    xmlSchema.getTopLevelObject(), e);
            throw new RuntimeException(e.getCause());
        }
    }

    public String jsonToXml(String json) {
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
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

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
            throw new RuntimeException(e);
        }
    }

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
