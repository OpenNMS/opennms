/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.collection.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;

import com.google.common.collect.Maps;

public class CollectionSetUtils {

    public static Map<String, Map<String, CollectionAttribute>> getAttributesByNameByGroup(CollectionSet collectionSet) {
        final Map<String, Map<String, CollectionAttribute>> attributesByNameByGroup = Maps.newHashMap();
        collectionSet.visit(new AbstractCollectionSetVisitor() {
            private String groupName = null;
            private Map<String, CollectionAttribute> attributesByName = Maps.newHashMap();

            @Override
            public void visitGroup(AttributeGroup group) {
                groupName = group.getName();
            }

            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                attributesByName.put(attribute.getName(), attribute);
            }

            @Override
            public void completeGroup(AttributeGroup group) {
                attributesByNameByGroup.put(groupName, attributesByName);
                attributesByName = Maps.newHashMap();
            }
        });
        return attributesByNameByGroup;
    }

    public static Map<String, CollectionAttribute> getAttributesByName(CollectionSet collectionSet) {
        final Map<String, CollectionAttribute> attributesByName = Maps.newHashMap();
        collectionSet.visit(new AbstractCollectionSetVisitor() {
            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                attributesByName.put(attribute.getName(), attribute);
            }
        });
        return attributesByName;
    }

    public static Map<String, CollectionResource> getResourcesByLabel(CollectionSet collectionSet) {
        final Map<String, CollectionResource> resourcesByLabel = Maps.newLinkedHashMap();
        collectionSet.visit(new AbstractCollectionSetVisitor() {
            @Override
            public void visitResource(CollectionResource resource) {
                resourcesByLabel.put(resource.getInterfaceLabel(), resource);
            }
        });
        return resourcesByLabel;
    }

    public static List<String> flatten(CollectionSet collectionSet) {
        final List<String> strings = new ArrayList<>();
        collectionSet.visit(new AbstractCollectionSetVisitor() {
            CollectionResource resource;
            AttributeGroup group;

            @Override
            public void visitResource(CollectionResource resource) {
                this.resource = resource;
            }
            @Override
            public void visitGroup(AttributeGroup group) {
                this.group = group;
            }
            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                strings.add(String.format("%s/%s/%s[%s,%s]", resource.getPath(), group.getName(),
                        attribute.getName(),attribute.getStringValue(),attribute.getNumericValue()));
            }
        });
        return strings;
    }
}
