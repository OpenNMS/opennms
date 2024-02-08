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
