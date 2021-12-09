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

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.walker.XmlSchemaAttrInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaRestriction;
import org.apache.ws.commons.schema.walker.XmlSchemaTypeInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaWalker;
import org.opennms.features.config.dao.api.ConfigItem;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.*;

import static org.apache.ws.commons.schema.constants.Constants.MetaDataConstants.EXTERNAL_ATTRIBUTES;

/**
 * Used to convert a XSD to a structure of {@link ConfigItem}s.
 * It usually uses with JaxbXmlConverter together.
 * @see JaxbXmlConverter
 */
public class XsdModelConverter extends NoopXmlSchemaVisitor {
    public static final String XML_ELEMENT_VALUE_BODY_TAG = "body-name";

    private final Map<QName, ConfigItem> configItemsByQName = new LinkedHashMap<>();
    private final List<ConfigItem> configItemStack = new LinkedList<>();
    private ConfigItem currentConfigItem;
    private XmlSchemaCollection collection;
    private Map<String, String> elementNameToValueNameMap = null;

    public XsdModelConverter(String xsdStr) {
        super();
        this.collection = this.convertToSchemaCollection(xsdStr);
    }

    public XmlSchemaCollection getCollection() {
        return collection;
    }

    private XmlSchemaCollection convertToSchemaCollection(String xsdStr) {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        try (StringReader reader = new StringReader(xsdStr)) {
            // no need to check read for StringReader if the source is a String
            schemaCollection.read(reader);
        }
        return schemaCollection;
    }

    public ConfigItem convert(String topLevelElement) {
        XmlSchemaWalker walker = new XmlSchemaWalker(collection, this);
        XmlSchemaElement rootEl = getElementOf(collection, topLevelElement);
        if (rootEl == null) {
            throw new RuntimeException("No element found with name: " + topLevelElement);
        }
        elementNameToValueNameMap = new HashMap<>();
        walker.walk(rootEl);
        return configItemsByQName.get(rootEl.getQName());
    }

    public Map<String, String> getElementNameToValueNameMap() {
        if (elementNameToValueNameMap == null) {
            throw new RuntimeException("Should call after convert.");
        }
        return elementNameToValueNameMap;
    }

    /**
     * It will read the ejaxb:body-name and put in elementNameToValueNameMap
     * @param xmlSchemaElement
     */
    private void handleExternalAttributes(XmlSchemaElement xmlSchemaElement) {
        if (xmlSchemaElement.getMetaInfoMap() == null) {
            return;
        }
        Object attributes = xmlSchemaElement.getMetaInfoMap().get(EXTERNAL_ATTRIBUTES);
        if (attributes instanceof Map) {
            ((Map) attributes).forEach((key, value) -> {
                if (value instanceof Node) {
                    Node attr = (Node) value;
                    if (XML_ELEMENT_VALUE_BODY_TAG.equals(attr.getLocalName())) {
                        String tmpName = xmlSchemaElement.getQName().getLocalPart();
                        elementNameToValueNameMap.computeIfAbsent(tmpName, elementName -> attr.getNodeValue());
                        return;
                    }
                }
            });
        }
    }

    @Override
    public void onEnterElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
        this.handleExternalAttributes(xmlSchemaElement);

        currentConfigItem = configItemsByQName.get(xmlSchemaElement.getQName());
        if (currentConfigItem == null) {
            ConfigItem.Type baseType = getConfigType(xmlSchemaTypeInfo.getBaseType().name());
            currentConfigItem = new ConfigItem();
            currentConfigItem.setName(xmlSchemaElement.getQName().getLocalPart());
            currentConfigItem.setDocumentation(getDocumentation(xmlSchemaElement.getAnnotation()));
            currentConfigItem.setSchemaRef(xmlSchemaElement.getQName().toString());
            currentConfigItem.setType(baseType);
            setRestrictions(currentConfigItem, xmlSchemaTypeInfo.getFacets());
            configItemsByQName.put(xmlSchemaElement.getQName(), currentConfigItem);
        }
        configItemStack.add(currentConfigItem);

//        FIXME: Requirements are attributes of the parent/child relations and are not directly on the objects themselves
//        // if there is a minimum, then the element is required
//        if (xmlSchemaElement.getMinOccurs() > 0) {
//            currentConfigItem.setRequired(true);
//        }

        ConfigItem configItemForParent = getConfigItemForParentElement();
        if (configItemForParent != null) {
            if (xmlSchemaElement.getMaxOccurs() > 1) {
                // There can be many elements defined, wrap it in an array
                ConfigItem configItemForArray = new ConfigItem();
                configItemForArray.setName(currentConfigItem.getName());
                configItemForArray.setSchemaRef(xmlSchemaElement.getQName().toString());
                configItemForArray.setType(ConfigItem.Type.ARRAY);
                configItemForArray.getChildren().add(currentConfigItem);
                configItemForParent.getChildren().add(configItemForArray);
            } else {
                // There may only be 1 element, add it directly to the parent as an object
                configItemForParent.getChildren().add(currentConfigItem);
            }
        }
    }

    private ConfigItem getConfigItemForParentElement() {
        if (configItemStack.size() < 2) {
            return null;
        }
        return configItemStack.get(configItemStack.size() - 2);
    }

    @Override
    public void onExitElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
        configItemStack.remove(configItemStack.size() - 1);
    }

    @Override
    public void onVisitAttribute(XmlSchemaElement xmlSchemaElement, XmlSchemaAttrInfo xmlSchemaAttrInfo) {
        currentConfigItem.getChildren().add(getConfigItemForAttribute(xmlSchemaAttrInfo));
        // Check current type -> needs to be an object to create children
        // Special case where parent is a simple type with an attribute
        if (currentConfigItem.isPrimitiveType()) {
            // Make a duplicate of the current item for the primitive type; current then becomes an object with children
            ConfigItem child = new ConfigItem();
            // special logic to handle Xml Value to attribute name mapping
            String bodyName = this.elementNameToValueNameMap.get(currentConfigItem.getName());
            child.setName(bodyName != null ? bodyName: currentConfigItem.getName());
            child.setType(currentConfigItem.getType());
            child.setSchemaRef(currentConfigItem.getSchemaRef());
            child.setRequired(currentConfigItem.isRequired());
            currentConfigItem.setType(ConfigItem.Type.OBJECT);
            currentConfigItem.setSchemaRef("");
            currentConfigItem.getChildren().add(child);
        }
    }

    private void handleStringRestrictions(ConfigItem item, HashMap<XmlSchemaRestriction.Type, List<XmlSchemaRestriction>> facets) {
        List<XmlSchemaRestriction> patternFacets = facets.get(XmlSchemaRestriction.Type.PATTERN);
        if (patternFacets != null && !patternFacets.isEmpty()) {
            XmlSchemaRestriction restriction = patternFacets.get(0);
            Object obj = restriction.getValue();
            if (obj instanceof String) {
                item.setPattern((String) obj);
            }
        }
    }

    private void handleOtherRestrictions(ConfigItem item, HashMap<XmlSchemaRestriction.Type, List<XmlSchemaRestriction>> facets) {
        List<XmlSchemaRestriction> minFacets = facets.get(XmlSchemaRestriction.Type.INCLUSIVE_MIN);
        if ((minFacets != null) && !minFacets.isEmpty()) {
            // Should only be one minimum specified
            XmlSchemaRestriction minRestriction = minFacets.get(0);
            Object minVal = minRestriction.getValue();
            if (minVal instanceof String) {
                item.setMin(Long.valueOf((String) minVal));
            }
        }

        List<XmlSchemaRestriction> maxFacets = facets.get(XmlSchemaRestriction.Type.INCLUSIVE_MAX);
        if ((maxFacets != null) && !maxFacets.isEmpty()) {
            // Should only be one minimum specified
            XmlSchemaRestriction maxRestriction = maxFacets.get(0);
            Object maxVal = maxRestriction.getValue();
            if (maxVal instanceof String) {
                item.setMax(Long.valueOf((String) maxVal));
            }
        }
    }

    private void setRestrictions(ConfigItem item, HashMap<XmlSchemaRestriction.Type, List<XmlSchemaRestriction>> facets) {
        if (facets == null) {
            return;
        }
        if (item.getType() == ConfigItem.Type.STRING) {
            this.handleStringRestrictions(item, facets);
        } else {
            this.handleOtherRestrictions(item, facets);
        }
    }

    private XmlSchemaElement getElementOf(XmlSchemaCollection collection, String name) {
        XmlSchemaElement elem = null;
        for (org.apache.ws.commons.schema.XmlSchema schema : collection.getXmlSchemas()) {
            elem = schema.getElementByName(name);
            if (elem != null) {
                break;
            }
        }
        return elem;
    }

    /**
     * Reading documentation from xsd annotation
     *
     * @param xmlSchemaAnnotation
     * @return
     */
    public String getDocumentation(XmlSchemaAnnotation xmlSchemaAnnotation) {
        if (xmlSchemaAnnotation != null) {
            for (XmlSchemaAnnotationItem item : xmlSchemaAnnotation.getItems()) {
                if (item instanceof XmlSchemaDocumentation) {
                    XmlSchemaDocumentation doc = (XmlSchemaDocumentation) item;
                    if (doc.getMarkup() != null) {
                        StringBuffer sb = new StringBuffer();
                        for (int i = 0; i < doc.getMarkup().getLength(); i++) {
                            sb.append(doc.getMarkup().item(i).getNodeValue());
                        }
                        return sb.toString();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Convert xsd attribute into ConfigItem
     *
     * @param xmlSchemaAttrInfo
     * @return
     */
    public ConfigItem getConfigItemForAttribute(XmlSchemaAttrInfo xmlSchemaAttrInfo) {
        ConfigItem configItem = new ConfigItem();
        configItem.setName(xmlSchemaAttrInfo.getAttribute().getName());
        if (xmlSchemaAttrInfo.getAttribute().getAnnotation() != null) {
            configItem.setDocumentation(getDocumentation(xmlSchemaAttrInfo.getAttribute().getAnnotation()));
        } else if (xmlSchemaAttrInfo.getAttribute().getSchemaType().getAnnotation() != null) {
            configItem.setDocumentation(getDocumentation(xmlSchemaAttrInfo.getAttribute().getSchemaType().getAnnotation()));
        }
        configItem.setType(getTypeForAttribute(xmlSchemaAttrInfo));
        configItem.setRequired("REQUIRED".equals(xmlSchemaAttrInfo.getAttribute().getUse().name()));
        configItem.setDefaultValue(xmlSchemaAttrInfo.getAttribute().getDefaultValue());
        configItem.setSchemaRef(xmlSchemaAttrInfo.getAttribute().getQName().toString());
        setRestrictions(configItem, xmlSchemaAttrInfo.getType() == null ? null : xmlSchemaAttrInfo.getType().getFacets());


        return configItem;
    }

    /**
     * Handle xsd type
     *
     * @param xmlSchemaAttrInfo
     * @return
     */
    public ConfigItem.Type getTypeForAttribute(XmlSchemaAttrInfo xmlSchemaAttrInfo) {
        String strType;
        ConfigItem.Type type = null;

        if(xmlSchemaAttrInfo.getAttribute().getSchemaType() == null && xmlSchemaAttrInfo.getType() == null){
            type = ConfigItem.Type.STRING;
        } else {
            if (xmlSchemaAttrInfo.getAttribute().getSchemaType().getQName() != null) {
                strType = xmlSchemaAttrInfo.getAttribute().getSchemaType().getQName().getLocalPart().toLowerCase();
                type = getConfigType(strType);
            }
            if (ConfigItem.Type.OBJECT == type || type == null) {
                strType = xmlSchemaAttrInfo.getType().getBaseType().name().toLowerCase();
                type = getConfigType(strType);
            }
        }
        return type;
    }

    private ConfigItem.Type getConfigType(String type) {
        switch (type.toLowerCase()) {
            case "double":
            case "decimal":
                return ConfigItem.Type.NUMBER;
            case "string":
            case "id":
            case "idref":
            case "token":
            case "anysimpletype":
                return ConfigItem.Type.STRING;
            case "boolean":
                return ConfigItem.Type.BOOLEAN;
            case "integer":
            case "int":
            case "short":
                return ConfigItem.Type.INTEGER;
            case "long":
                return ConfigItem.Type.LONG;
            case "positiveinteger":
                return ConfigItem.Type.POSITIVE_INTEGER;
            case "negativeinteger":
                return ConfigItem.Type.NEGATIVE_INTEGER;
            case "nonnegativeinteger":
            case "unsignedbyte":
            case "unsignedshort":
            case "unsignedint":
                return ConfigItem.Type.NON_NEGATIVE_INTEGER;
            case "date":
                return ConfigItem.Type.DATE;
            case "datetime":
                return ConfigItem.Type.DATE_TIME;
            default:
                // both anytype and complextype will be detected later
                return ConfigItem.Type.OBJECT;
        }
    }
}