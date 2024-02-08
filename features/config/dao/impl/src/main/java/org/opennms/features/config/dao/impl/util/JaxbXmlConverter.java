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
import org.opennms.features.config.exception.SchemaConversionException;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * It handles xml <> json conventions base on xsd.
 */
public class JaxbXmlConverter implements ConfigConverter {
    private static final Logger LOG = LoggerFactory.getLogger(JaxbXmlConverter.class);
    public static final String VALUE_TAG = "__VALUE__";

    private final DynamicJAXBContext jaxbContext;
    private final XmlSchema xmlSchema;
    private String xsdName;
    private String rootElement;
    private Map<String, String> elementNameToValueNameMap;

    public JaxbXmlConverter(final String xsdName,
                            final String rootElement,
                            Map<String, String> elementNameToValueNameMap)
            throws IOException {
        this.xsdName = Objects.requireNonNull(xsdName);
        this.rootElement = Objects.requireNonNull(rootElement);
        this.elementNameToValueNameMap = elementNameToValueNameMap;
        this.xmlSchema = this.readXmlSchema();
        this.jaxbContext = getDynamicJAXBContextForService(xmlSchema);
    }

    /**
     * It searches the xsd defined in configuration class and load into schema.
     *
     * @return XmlSchema
     * @throws IOException
     */
    private XmlSchema readXmlSchema() throws IOException {
        String xsdStr = Resources.toString(XsdHelper.getSchemaPath(xsdName), StandardCharsets.UTF_8);
        final XsdModelConverter xsdModelConverter = new XsdModelConverter(xsdStr);
        final XmlSchemaCollection schemaCollection = xsdModelConverter.getCollection();
        // Grab the first namespace that includes 'opennms', sort for predictability
        List<String> namespaces = Arrays.stream(schemaCollection.getXmlSchemas())
                .map(org.apache.ws.commons.schema.XmlSchema::getTargetNamespace)
                .filter(targetNamespace -> targetNamespace.contains("opennms")).collect(Collectors.toList());

        if (namespaces.size() != 1) {
            LOG.error("XSD must contain one 'opennms' namespace!");
            throw new SchemaConversionException("XSD must contain one 'opennms' namespace!");
        }

        return new XmlSchema(xsdStr, namespaces.get(0), rootElement);
    }

    public String getRootElement() {
        return rootElement;
    }


    /**
     * Convert xml to json. If elementNameToValueNameMap is not null, it will setup XmlValue attribute name properly
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

            //dirty tricks to detect xml value (use for JSONObject remove)
            m.setProperty(MarshallerProperties.JSON_VALUE_WRAPPER, VALUE_TAG);

            final StringWriter writer = new StringWriter();
            m.marshal(entity, writer);
            String jsonStr = writer.toString();
            if (jsonStr.indexOf(VALUE_TAG) != -1) {
                JSONObject json = new JSONObject(jsonStr);
                if (!this.elementNameToValueNameMap.isEmpty()) {
                    json = this.replaceXmlValueAttributeName(json);
                }
                json = this.removeEmptyValueTag(json);
                return json.toString();
            } else {
                return jsonStr;
            }
        } catch (JAXBException | SAXException e) {
            throw new SchemaConversionException(sourceXml, e);
        }
    }

    /**
     * Remove empty XmlValue tag
     *
     * @param json
     * @return json without empty value tag
     */
    public JSONObject removeEmptyValueTag(JSONObject json) {
        Objects.requireNonNull(json);
        final Set<String> keys = new HashSet<>(json.keySet());
        for (String key : keys) {
            Object value;
            synchronized (this) {
                if (!json.has(key))
                    continue;
                value = json.get(key);
            }
            // trim is important, it always contains return character
            if (VALUE_TAG.equals(key) && value instanceof String && ((String) value).trim().isEmpty()) {
                json.remove(key);
            } else if (value instanceof JSONObject) {
                removeEmptyValueTag((JSONObject) value);
            } else if (value instanceof JSONArray) {
                ((JSONArray) value).forEach(item -> {
                    if (item instanceof JSONObject) {
                        this.removeEmptyValueTag((JSONObject) item);
                    }
                });
            }
        }
        return json;
    }

    /**
     * Help to replace XmlValue tag (value) to expected attributeName
     *
     * @param json
     * @return json with replaced value
     */
    private JSONObject replaceXmlValueAttributeName(JSONObject json) {
        this.elementNameToValueNameMap.forEach((elementName, valueName) -> {
            if (json.has(elementName)) {
                Object value = json.get(elementName);
                if (value instanceof JSONArray) {
                    JSONArray tmpList = (JSONArray) value;
                    tmpList.forEach(item -> {
                        if (item instanceof JSONObject) {
                            this.replaceKey((JSONObject) item, VALUE_TAG, valueName);
                        }
                    });
                } else if (value instanceof JSONObject) {
                    this.replaceKey((JSONObject) value, VALUE_TAG, valueName);
                }
            }
        });
        // loop through children elements
        json.keySet().forEach(key -> {
            Object value = json.get(key);
            if (value instanceof JSONObject) {
                this.replaceXmlValueAttributeName((JSONObject) value);
            } else if (value instanceof JSONArray) {
                ((JSONArray) value).forEach(arrayItem -> {
                    if (arrayItem instanceof JSONObject) {
                        this.replaceXmlValueAttributeName((JSONObject) arrayItem);
                    }
                });
            }
        });
        return json;
    }

    private void replaceKey(JSONObject json, String oldKey, String newKey) {
        Object value = json.remove(oldKey);
        json.put(newKey, value);
    }

    private DynamicJAXBContext getDynamicJAXBContextForService(XmlSchema xmlSchema) {
        final String xsd = xmlSchema.getXsdContent();

        try (InputStream is = new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8))) {
            return DynamicJAXBContextFactory.createContextFromXSD(is, null, null, null);
        } catch (JAXBException | IOException e) {
            throw new SchemaConversionException(xsd, e);
        }
    }

    public XmlSchema getXmlSchema() {
        return xmlSchema;
    }
}
