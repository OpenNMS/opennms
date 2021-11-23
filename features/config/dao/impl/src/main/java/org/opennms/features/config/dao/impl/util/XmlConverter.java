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
import org.eclipse.persistence.dynamic.DynamicEntity;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContext;
import org.eclipse.persistence.jaxb.dynamic.DynamicJAXBContextFactory;
import org.eclipse.persistence.oxm.MediaType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.dao.api.ConfigConverter;
import org.opennms.features.config.dao.api.ConfigDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * It handles all kinds of xml <> json conventions.
 */
public class XmlConverter implements ConfigConverter {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigConverter.class);
    public static final String __VALUE__TAG = "__VALUE__";

    private final DynamicJAXBContext jaxbContext;
    private XmlMapper xmlMapper;
    private final XmlSchema xmlSchema;
    private String xsdName;
    private String rootElement;
    private Map<String, String> elementNameToValueNameMap;

    public XmlConverter(final String xsdName,
                        final String rootElement,
                        Map<String, String> elementNameToValueNameMap)
            throws IOException {
        this.xsdName = Objects.requireNonNull(xsdName);
        this.rootElement = Objects.requireNonNull(rootElement);
        this.elementNameToValueNameMap = elementNameToValueNameMap;
        this.xmlSchema = this.readXmlSchema();
        //TODO: remove xmlMapper
        this.xmlMapper = new XmlMapper(xmlSchema);
        this.jaxbContext = getDynamicJAXBContextForService(xmlSchema);
    }

    /**
     * It searches the xsd defined in configuration class and load into schema.
     *
     * @return XmlSchema
     * @throws IOException
     */
    private XmlSchema readXmlSchema() throws IOException {
        String xsdStr = Resources.toString(SchemaUtil.getSchemaPath(xsdName), StandardCharsets.UTF_8);
        final XsdModelConverter xsdModelConverter = new XsdModelConverter(xsdStr);
        final XmlSchemaCollection schemaCollection = xsdModelConverter.getCollection();
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


    /**
     * Convert xml to json
     *
     * @param sourceXml
     * @return json string
     */
    @Override

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
            if (elementNameToValueNameMap != null) {
                //dirty tricks to remove xml value (use for JSONObject remove)
                m.setProperty(MarshallerProperties.JSON_VALUE_WRAPPER, __VALUE__TAG);
            }

            final StringWriter writer = new StringWriter();
            m.marshal(entity, writer);
            String jsonStr = writer.toString();
            if (elementNameToValueNameMap != null && jsonStr.indexOf(__VALUE__TAG) != -1) {
                return this.handleElementBody(jsonStr);
            } else {
                return jsonStr;
            }
        } catch (JAXBException | SAXException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void replaceKey(JSONObject json, String oldKey, String newKey){
        Object value = json.remove(oldKey);
        json.put(newKey, value);
    }

    //TODO: need more data for testing
    private String handleElementBody(String jsonStr){
        JSONObject json = new JSONObject(jsonStr);
        this.elementNameToValueNameMap.forEach((elementName,valueName)->{
            if(json.has(elementName)){
                Object value = json.get(elementName);
                if(value instanceof JSONArray){
                    JSONArray tmpList = (JSONArray) value;
                    tmpList.forEach(item->{
                        if(item instanceof JSONObject){
                            replaceKey((JSONObject)item, __VALUE__TAG, valueName);
                        }
                    });

                } else if (value instanceof JSONObject){
                    replaceKey((JSONObject)value, __VALUE__TAG, valueName);
                }
            }
        });
//        if (json.has(__VALUE__TAG)) {
//            String value = json.getString(__VALUE__TAG);
//            if (value != null && value.trim().length() == 0) {
//                json.remove(__VALUE__TAG);
//            }
//        }
        return json.toString();

    }

    @Override
    public String jsonToXml(final String jsonStr) {
        return xmlMapper.jsonToXml(jsonStr);
    }

    private DynamicJAXBContext getDynamicJAXBContextForService(XmlSchema xmlSchema) {
        final String xsd = xmlSchema.getXsdContent();

        try (InputStream is = new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8))) {
            return DynamicJAXBContextFactory.createContextFromXSD(is, null, null, null);
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public XmlSchema getXmlSchema() {
        return xmlSchema;
    }
}