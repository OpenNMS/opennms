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
    private String unmodifiedInstance;
    private final String type;
    private final Map<String,String> attributes = new HashMap<String,String>();
    
    public MockCollectionResource(ResourcePath parent, String instance, String type) {
        this(parent, instance, instance, type);
    }

    public MockCollectionResource(ResourcePath parent, String instance, String unmodifiedInstance, String type) {
        this.parent = parent;
        this.instance = sanitizeInstance(instance);
        this.unmodifiedInstance = unmodifiedInstance;
        this.type = type;
    }

    /**
     * Copied from GenericTypeResource
     */
    public static String sanitizeInstance(String instance) {
        return instance.replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_");
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
    public String getUnmodifiedInstance() {
        return unmodifiedInstance;
    }

    public void setUnmodifiedInstance(String instance) {
        this.unmodifiedInstance = instance;
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
