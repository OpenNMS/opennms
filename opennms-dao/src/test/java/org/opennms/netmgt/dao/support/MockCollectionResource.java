/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.model.ResourcePath;

/**
 * MockCollectionResource
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class MockCollectionResource implements CollectionResource {
    
    private final ResourcePath parent;
    private String instance;
    private final String type;
    private final Map<String,String> attributes = new HashMap<String,String>();
    
    public MockCollectionResource(ResourcePath parent, String instance, String type) {
        this.parent = parent;
        this.instance = instance;
        this.type = type;
    }

    @Override
    public String getOwnerName() {
        return null;
    }

    @Override
    public ResourcePath getPath() {
        return null;
    }

    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return false;
    }

    @Override
    public boolean rescanNeeded() {
        return false;
    }

    @Override
    public void visit(CollectionSetVisitor visitor) {
        for (Entry<String,String> entry : attributes.entrySet()) {
            final CollectionResource resource = this;
            final String attrName = entry.getKey();
            final String attrValue = entry.getValue();
            CollectionAttribute attribute = new CollectionAttribute() {
                @Override
                public CollectionResource getResource() { return resource; }
                @Override
                public String getStringValue() { return attrValue; }
                @Override
                public Double getNumericValue() {
                    try {
                        return Double.parseDouble(attrValue);
                    } catch (NumberFormatException|NullPointerException e) {
                        return null;
                    }
                }
                @Override
                public String getName() { return attrName; }
                @Override
                public void storeAttribute(Persister persister) {}
                @Override
                public boolean shouldPersist(ServiceParameters params) { return true; }
                @Override
                public CollectionAttributeType getAttributeType() { return null; }
                @Override
                public void visit(CollectionSetVisitor visitor) { }
                @Override
                public AttributeType getType() { return AttributeType.STRING; }
                @Override
                public String getMetricIdentifier() { return "MOCK_"+getName(); }
            };
            visitor.visitAttribute(attribute);
        }
    }

    @Override
    public String getResourceTypeName() {
        return type;
    }

    @Override
    public ResourcePath getParent() {
        return parent;
    }

    @Override
    public String getInstance() {
        return instance;
    }
    
    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    public String getInterfaceLabel() {
        return null;
    }
    
    public Map<String,String> getAttributeMap() {
        return attributes;
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

}
