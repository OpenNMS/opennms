/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.cm.svc;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.walker.XmlSchemaAttrInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaTypeInfo;
import org.apache.ws.commons.schema.walker.XmlSchemaWalker;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

public class SwaggerConverter extends NoopXmlSchemaVisitor {

    private OpenAPI openAPI = new OpenAPI();

    public OpenAPI convert(XmlSchemaCollection collection) {
        Components components = new Components();
        openAPI.setComponents(components);

        XmlSchemaWalker walker = new XmlSchemaWalker(collection, this);
        // FIXME: This would need to be provided as well
        walker.walk(getElementOf(collection, "VacuumdConfiguration"));

        schemaMap.forEach((k,v) -> components.addSchemas(k.getLocalPart(),v));
        return openAPI;
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

    Map<QName, ObjectSchema> schemaMap = new LinkedHashMap<>();
    List<ObjectSchema> schemaStack = new LinkedList<>();
    ObjectSchema schemaForCurrentElement;

    @Override
    public void onEnterElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
        System.out.println("onEnterElement(" + xmlSchemaElement.getQName() + ")");
        System.out.printf("\tbase type is: %s min-occurs: %d, max-occurs: %d\n",
                xmlSchemaTypeInfo.getBaseType().name(), xmlSchemaElement.getMinOccurs(), xmlSchemaElement.getMaxOccurs());

        schemaForCurrentElement = schemaMap.get(xmlSchemaElement.getQName());
        if (schemaForCurrentElement == null) {
            schemaForCurrentElement = new ObjectSchema();
            schemaMap.put(xmlSchemaElement.getQName(), schemaForCurrentElement);
        }
        schemaStack.add(schemaForCurrentElement);

        ObjectSchema schemaForParent = getSchemaForParentElement();
        if (schemaForParent != null) {
            ObjectSchema schemaForRef = new ObjectSchema();
            schemaForRef.set$ref("#/components/schemas/" + xmlSchemaElement.getQName().getLocalPart());

            if (xmlSchemaElement.getMaxOccurs() == 1) {
                // We expect one of these elements to be defined in the parent
                schemaForParent.addProperties(xmlSchemaElement.getQName().getLocalPart(),schemaForRef);
                if (xmlSchemaElement.getMinOccurs() == 1) {
                    schemaForParent.addRequiredItem(xmlSchemaElement.getQName().getLocalPart());
                }
            } else if (xmlSchemaElement.getMaxOccurs() > 1) {
                // We expect many of these elements to be defined in the parent as the form of an array
                ArraySchema schemaForArray = new ArraySchema();
                schemaForArray.setMinItems((int)xmlSchemaElement.getMinOccurs());
                if (xmlSchemaElement.getMaxOccurs() < Long.MAX_VALUE) {
                    schemaForArray.setMaxItems((int)xmlSchemaElement.getMaxOccurs());
                }
                schemaForArray.setItems(schemaForRef);

                schemaForParent.addProperties(xmlSchemaElement.getQName().getLocalPart(),  schemaForArray);
                if (xmlSchemaElement.getMinOccurs() > 0) {
                    schemaForParent.addRequiredItem(xmlSchemaElement.getQName().getLocalPart());
                }
            }
        }
    }

    ObjectSchema getSchemaForParentElement() {
        if (schemaStack.size() < 2) {
            return null;
        }
        return schemaStack.get(schemaStack.size() - 2);
    }

    @Override
    public void onExitElement(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo, boolean b) {
        System.out.println("onExitElement(" + xmlSchemaElement.getQName() + ")");
        schemaStack.remove(schemaStack.size()-1);
    }

    @Override
    public void onVisitAttribute(XmlSchemaElement xmlSchemaElement, XmlSchemaAttrInfo xmlSchemaAttrInfo) {
        System.out.println("onVisitAttribute(" + xmlSchemaElement.getQName() + " )");
        System.out.printf("\t%s has type: %s\n", xmlSchemaAttrInfo.getAttribute().getName(), xmlSchemaAttrInfo.getType().getBaseType().name());

        schemaForCurrentElement.addProperties(xmlSchemaAttrInfo.getAttribute().getName(), getSchemaForAttribute(xmlSchemaAttrInfo));
    }

    @Override
    public void onEndAttributes(XmlSchemaElement xmlSchemaElement, XmlSchemaTypeInfo xmlSchemaTypeInfo) {
        System.out.println("onEndAttributes(" + xmlSchemaElement.getQName() + ")");
    }

    public static Schema<?> getSchemaForAttribute(XmlSchemaAttrInfo xmlSchemaAttrInfo) {
        String type = xmlSchemaAttrInfo.getType().getBaseType().name().toLowerCase();
        switch (type) {
            case "double":
            case "decimal":
                return new NumberSchema();
            case "string":
                return new StringSchema();
            case "boolean":
                return new BooleanSchema();
            case "integer":
            case "int":
                return new IntegerSchema();
            case "long":
                NumberSchema schema = new NumberSchema();
                schema.setFormat("int64");
                return schema;
            default:
                throw new UnsupportedOperationException("Unsupported attribute type: " + type);
        }
    }
}
