/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2021 The OpenNMS Group, Inc.
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
 ******************************************************************************/

package org.opennms.features.config.dao.impl.util;

import com.google.common.io.Resources;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * It handles all kinds of xml <> json conventions.
 */
public class XmlConverter implements ConfigConverter {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigConverter.class);
    private XmlMapper xmlMapper;
    private XmlSchema xmlSchema;
    private String xsdName;
    private String rootElement;

    public XmlConverter(final String xsdName,
                        final String rootElement)
            throws IOException {
        this.xsdName = Objects.requireNonNull(xsdName);
        this.rootElement = Objects.requireNonNull(rootElement);
        this.xmlSchema = this.readXmlSchema();
        this.xmlMapper = new XmlMapper(xmlSchema);
    }

    /**
     * It searches the xsd defined in configuration class and load into schema.
     *
     * @return XmlSchema
     * @throws IOException
     */
    private XmlSchema readXmlSchema() throws IOException {
        String xsdStr = Resources.toString(SchemaUtil.getSchemaPath(xsdName), StandardCharsets.UTF_8);
        final XsdModelConverter xsdModelConverter = new XsdModelConverter();
        final XmlSchemaCollection schemaCollection = xsdModelConverter.convertToSchemaCollection(xsdStr);
        // Grab the first namespace that includes 'opennms', sort for predictability
        List<String> namespaces = Arrays.stream(schemaCollection.getXmlSchemas())
                .map(org.apache.ws.commons.schema.XmlSchema::getTargetNamespace)
                .filter(targetNamespace -> targetNamespace.contains("opennms")).collect(Collectors.toList());

        if (namespaces.size() != 1) {
            LOG.error("XSD must contain one 'opennms' namespaces!");
            throw new IllegalArgumentException("XSD must contain one 'opennms' namespaces!");
        }

        return new XmlSchema(xsdStr, namespaces.get(0), rootElement);
    }

    public String getRootElement() {
        return rootElement;
    }

    @Override
    public String getRawSchema() {
        return this.xmlSchema.getXsdContent();
    }

    @Override
    public String xmlToJson(final String xmlStr) {
        return xmlMapper.xmlToJson(xmlStr);
    }

    @Override
    public String jsonToXml(final String jsonStr) {
        return xmlMapper.jsonToXml(jsonStr);
    }
}