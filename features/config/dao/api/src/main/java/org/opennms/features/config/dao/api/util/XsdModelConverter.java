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
package org.opennms.features.config.dao.api.util;

import java.io.StringReader;
import java.util.*;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.walker.XmlSchemaAttrInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaRestriction;
import org.apache.ws.commons.schema.walker.XmlSchemaTypeInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaWalker;
import org.opennms.features.config.dao.api.ConfigItem;

/**
 * Used to convert a XSD to a structure of {@link ConfigItem}s.
 */
public class XsdModelConverter extends NoopXmlSchemaVisitor {

    XmlSchemaCollection schemaCollection;
    final Map<QName, ConfigItem> configItemsByQName = new LinkedHashMap<>();
    final List<ConfigItem> configItemStack = new LinkedList<>();
    ConfigItem currentConfigItem;

    public XmlSchemaCollection convertToSchemaCollection(String xsdStr) {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        StringReader reader = new StringReader(xsdStr);
        try {
            // no need to check read for StringReader if the source is a String
            schemaCollection.read(reader);
        } finally {
            reader.close();
        }
        return schemaCollection;
    }

    public ConfigItem convert(XmlSchemaCollection collection, String topLevelElement) {
        XmlSchemaWalker walker = new XmlSchemaWalker(collection, this);
        XmlSchemaElement rootEl = getElementOf(collection, topLevelElement);
        if (rootEl == null) {
            throw new RuntimeException("No element found with name: " + topLevelElement);
        }
        walker.walk(rootEl);
        return configItemsByQName.get(rootEl.getQName());
    }

    @Override
    public void onEnterElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
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
        if (currentConfigItem.isPrimitiveType(currentConfigItem.getType())) {
            // Make a duplicate of the current item for the primitive type; current then becomes an object with children
            ConfigItem child = new ConfigItem();
            child.setName(currentConfigItem.getName());
            child.setType(currentConfigItem.getType());
            child.setSchemaRef(currentConfigItem.getSchemaRef());
            child.setRequired(currentConfigItem.isRequired());

            currentConfigItem.setType(ConfigItem.Type.OBJECT);
            currentConfigItem.setSchemaRef("");
            currentConfigItem.getChildren().add(child);
        }
    }

    private static void setRestrictions(ConfigItem item, HashMap<XmlSchemaRestriction.Type, List<XmlSchemaRestriction>> facets) {
        if (facets != null) {
            List<XmlSchemaRestriction> minfacets = facets.get(XmlSchemaRestriction.Type.INCLUSIVE_MIN);
            if ((minfacets != null) && (minfacets.size() > 0)) {
                // Should only be one minimum specified
                XmlSchemaRestriction minRestriction = minfacets.get(0);
                Object minVal = minRestriction.getValue();
                if (minVal instanceof String) {
                    String minString = (String) minVal;
                    long minLong = Long.valueOf(minString);
                    item.setMin(minLong);
                }
            }

            List<XmlSchemaRestriction> maxfacets = facets.get(XmlSchemaRestriction.Type.INCLUSIVE_MAX);
            if ((maxfacets != null) && (maxfacets.size() > 0)) {
                // Should only be one minimum specified
                XmlSchemaRestriction maxRestriction = maxfacets.get(0);
                Object maxVal = maxRestriction.getValue();
                if (maxVal instanceof String) {
                    String maxString = (String) maxVal;
                    long maxLong = Long.valueOf(maxString);
                    if (maxLong != Integer.MAX_VALUE) {
                        item.setMax(maxLong);
                    }
                }
            }

        }
    }

    private static XmlSchemaElement getElementOf(XmlSchemaCollection collection, String name) {
        XmlSchemaElement elem = null;
        for (XmlSchema schema : collection.getXmlSchemas()) {
            elem = schema.getElementByName(name);
            if (elem != null) {
                break;
            }
        }
        return elem;
    }

    /**
     * Reading documentation from xsd annotation
     * @param xmlSchemaAnnotation
     * @return
     */
    public static String getDocumentation(XmlSchemaAnnotation xmlSchemaAnnotation){
        if(xmlSchemaAnnotation != null){
            for(XmlSchemaAnnotationItem item: xmlSchemaAnnotation.getItems()){
                if (item instanceof XmlSchemaDocumentation) {
                    XmlSchemaDocumentation doc = (XmlSchemaDocumentation) item;
                    if ( doc.getMarkup() != null ){
                        StringBuffer sb = new StringBuffer();
                        for(int i = 0 ; i < doc.getMarkup().getLength() ; i++){
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
     * @param xmlSchemaAttrInfo
     * @return
     */
    public static ConfigItem getConfigItemForAttribute(XmlSchemaAttrInfo xmlSchemaAttrInfo) {
        ConfigItem configItem = new ConfigItem();
        configItem.setName(xmlSchemaAttrInfo.getAttribute().getName());
        configItem.setDocumentation(getDocumentation(xmlSchemaAttrInfo.getAttribute().getAnnotation()));
        configItem.setType(getTypeForAttribute(xmlSchemaAttrInfo));
        configItem.setRequired("REQUIRED".equals(xmlSchemaAttrInfo.getAttribute().getUse()));
        configItem.setDefaultValue(xmlSchemaAttrInfo.getAttribute().getDefaultValue());
        configItem.setSchemaRef(xmlSchemaAttrInfo.getAttribute().getQName().toString());
        setRestrictions(configItem, xmlSchemaAttrInfo.getType().getFacets());

        return configItem;
    }

    /**
     * Handle xsd type
     * @param xmlSchemaAttrInfo
     * @return
     */
    public static ConfigItem.Type getTypeForAttribute(XmlSchemaAttrInfo xmlSchemaAttrInfo) {
        String strType;
        ConfigItem.Type type = null;

        if(xmlSchemaAttrInfo.getAttribute().getSchemaType().getQName() != null) {
            strType = xmlSchemaAttrInfo.getAttribute().getSchemaType().getQName().getLocalPart().toLowerCase();
            type = getConfigType(strType);
        }
        if(ConfigItem.Type.OBJECT == type || type == null){
            strType = xmlSchemaAttrInfo.getType().getBaseType().name().toLowerCase();
            type = getConfigType(strType);
        }
        return type;
    }

    private static ConfigItem.Type getConfigType(String type) {
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
