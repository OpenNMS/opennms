/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.persistence.newts;

import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.collection.support.MultiResourceCollectionSet;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Utility class used to make it easier to build {@link org.opennms.netmgt.collection.api.CollectionSet}
 * objects for the purpose of testing a {@link org.opennms.netmgt.collection.api.Persister}.
 *
 * @author jwhite
 */
class CollectionSetBuilder {
    private final Map<Path, SampleCollectionResource> m_resourcesByPath = Maps.newLinkedHashMap();
    private Date m_timestamp = new Date();

    public CollectionSetBuilder withSample(Path resourcePath, String groupName, String attributeName, String type, Number value) {
        SampleCollectionResource resource = m_resourcesByPath.get(resourcePath);
        if (resource == null) {
            resource = new SampleCollectionResource(resourcePath);
            m_resourcesByPath.put(resourcePath, resource);
        }
        resource.addSample(groupName, attributeName, type, value);
        return this;
    }

    public CollectionSetBuilder withTimestamp(Date timestamp) {
        m_timestamp = timestamp;
        return this;
    }

    public CollectionSet build() {
        MultiResourceCollectionSet<SampleCollectionResource> collectionSet = new MultiResourceCollectionSet<SampleCollectionResource>() {};
        collectionSet.setCollectionResources(Sets.newLinkedHashSet(m_resourcesByPath.values()));
        collectionSet.setCollectionTimestamp(m_timestamp);
        return collectionSet;
    }

    private static class SampleCollectionResource implements CollectionResource {
        private final Path m_path;
        private final Map<String, AttributeGroup> m_groupsByName = Maps.newLinkedHashMap();

        public SampleCollectionResource(Path path) {
            m_path = path;
        }

        public void addSample(String groupName, String attributeName, String type, Number value) {
            AttributeGroup group = m_groupsByName.get(groupName);
            if (group == null) {
                AttributeGroupType groupType = new AttributeGroupType(groupName, AttributeGroupType.IF_TYPE_IGNORE);
                group = new AttributeGroup(this, groupType);
                m_groupsByName.put(groupName, group);
            }

            SampleCollectionAttributeType attrType = new SampleCollectionAttributeType(group.getGroupType(), attributeName);
            CollectionAttribute attr = new SampleCollectionAttribute(attrType, this, value, attributeName);
            group.addAttribute(attr);
        }

        @Override
        public String getOwnerName() {
            return null;
        }

        @Override
        public Path getPath() {
            return m_path;
        }

        @Override
        public void visit(CollectionSetVisitor visitor) {
            visitor.visitResource(this);
            for (AttributeGroup group: m_groupsByName.values()) {
                group.visit(visitor);
            }
            visitor.completeResource(this);
        }

        @Override
        public boolean shouldPersist(ServiceParameters params) {
            return true;
        }

        @Override
        public boolean rescanNeeded() {
            return false;
        }

        @Override
        public String getResourceTypeName() {
            return null;
        }

        @Override
        public String getParent() {
            return null;
        }

        @Override
        public String getInstance() {
            return null;
        }

        @Override
        public String getInterfaceLabel() {
            return null;
        }

        @Override
        public TimeKeeper getTimeKeeper() {
            return null;
        }
    }

    private static class SampleCollectionAttributeType extends AbstractCollectionAttributeType {
        private final String m_name;

        public SampleCollectionAttributeType(AttributeGroupType groupType, String name) {
            super(groupType);
            m_name = name;
        }

        @Override
        public String getType() {
            return AttributeGroupType.IF_TYPE_ALL;
        }

        @Override
        public String getName() {
            return m_name;
        }

        @Override
        public void storeAttribute(CollectionAttribute attribute, Persister persister) {
            persister.persistNumericAttribute(attribute);
        }
    }

    private static class SampleCollectionAttribute extends AbstractCollectionAttribute {
        private final Number m_value;
        private final String m_metricIdentifier;

        public SampleCollectionAttribute(SampleCollectionAttributeType attribType, CollectionResource resource, Number value, String metricIdentifier) {
            super(attribType, resource);
            m_value = value;
            m_metricIdentifier = metricIdentifier;
        }

        @Override
        public String getMetricIdentifier() {
            return m_metricIdentifier;
        }

        @Override
        public Number getNumericValue() {
            return m_value;
        }

        @Override
        public String getStringValue() {
            return null;
        }
    }
}
