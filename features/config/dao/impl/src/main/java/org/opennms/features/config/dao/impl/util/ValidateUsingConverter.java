/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ValidationSchema;
import org.opennms.features.config.dao.api.XmlSchema;
import org.opennms.features.config.dao.api.XmlValidationSchema;
import org.opennms.features.config.dao.api.util.XsdModelConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;

/**
 * It handles all kinds of xml <> json conventions
 *
 */
public class ValidateUsingConverter implements ConfigConverter {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigConverter.class);
    private XmlMapper xmlMapper;
    private String xsdName;
    private String rootElement;
    private XmlValidationSchema validationSchema;
    private SCHEMA_TYPE schemaType = SCHEMA_TYPE.XML;

    public ValidateUsingConverter(final String xsdName,
                                  final String rootElement)
            throws IllegalArgumentException, IOException, JAXBException {
        this.xsdName = Objects.requireNonNull(xsdName);
        this.rootElement = Objects.requireNonNull(rootElement);
        this.validationSchema = this.readXmlSchema();
        this.xmlMapper = new XmlMapper(validationSchema.getSchema());
    }

    /**
     * For object mapper use
     * @param xsdName
     * @param rootElement
     * @param xmlAccessorType
     * @param validationSchema
     * @throws JAXBException
     */
    @JsonCreator
    public ValidateUsingConverter(
            @JsonProperty("xsdName") String xsdName, @JsonProperty("rootElement") String rootElement,
            @JsonProperty("xmlAccessorType") XmlAccessType xmlAccessorType,
            @JsonProperty("validationSchema") XmlValidationSchema validationSchema) throws JAXBException {

        this.xsdName = xsdName;
        this.rootElement = rootElement;
        this.validationSchema = validationSchema;
        this.xmlMapper = new XmlMapper(validationSchema.getSchema());
    }
    /**
     * It searches the xsd defined in configuration class and load into schema.
     *
     * @return ServiceSchema with xsds
     * @throws IOException
     */
    private XmlValidationSchema readXmlSchema() throws IOException {
        String xsdStr = Resources.toString(this.getSchemaPath(), StandardCharsets.UTF_8);
        final XsdModelConverter xsdModelConverter = new XsdModelConverter();
        final XmlSchemaCollection schemaCollection = xsdModelConverter.convertToSchemaCollection(xsdStr);
        // Grab the first namespace that includes 'opennms', sort for predictability
        List<String> namespaces = Arrays.stream(schemaCollection.getXmlSchemas())
                .map(org.apache.ws.commons.schema.XmlSchema::getTargetNamespace)
                .filter(targetNamespace -> targetNamespace.contains("opennms")).collect(Collectors.toList());

        if (namespaces.size() != 1) {
            LOG.error("XSD must contain one 'opennms' namespaces! " + this.validationSchema);
            throw new IllegalArgumentException("XSD must contain one 'opennms' namespaces!");
        }

        XmlSchema xmlSchema = new XmlSchema(xsdStr, namespaces.get(0), rootElement);
        return new XmlValidationSchema(xmlSchema);
    }

    public String getRootElement() {
        return rootElement;
    }

    /**
     * It will search xsds first, otherwise it will search across classpath
     *
     * @return URL of the xsd file
     */
    @Override
    @JsonIgnore
    public URL getSchemaPath() throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource("xsds/" + xsdName);
        if (url == null) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:**/" + xsdName);
            if (resources != null && resources.length > 0)
                url = resources[0].getURL();
        }
        return url;
    }

    @Override
    public SCHEMA_TYPE getSchemaType() {
        return schemaType;
    }

    @Override
    public String getRawSchema() {
        return this.validationSchema.getSchema().getXsdContent();
    }

    @Override
    public boolean validate(String config, SCHEMA_TYPE type) throws RuntimeException {
        if(SCHEMA_TYPE.XML == type) {
            return xmlMapper.validate(config);
        } else {
            throw new IllegalArgumentException("Implement me");
        }
    }

    @Override
    public String xmlToJson(final String xmlStr) {
        return xmlMapper.xmlToJson(xmlStr);
    }

    @Override
    public String jsonToXml(final String jsonStr) {
        return xmlMapper.jsonToXml(jsonStr);
    }

    @Override
    public ValidationSchema<?> getValidationSchema() {
        return validationSchema;
    }
}
