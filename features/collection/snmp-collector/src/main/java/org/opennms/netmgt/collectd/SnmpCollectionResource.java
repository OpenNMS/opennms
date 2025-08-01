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
package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.NumericCollectionAttributeType;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract SnmpCollectionResource class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class SnmpCollectionResource implements CollectionResource {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectionResource.class);
    
    private final ResourceType m_resourceType;

    private final Map<AttributeGroupType, AttributeGroup> m_groups = new HashMap<AttributeGroupType, AttributeGroup>();

    /**
     * <p>Constructor for SnmpCollectionResource.</p>
     *
     * @param def a {@link org.opennms.netmgt.collectd.ResourceType} object.
     */
    public SnmpCollectionResource(final ResourceType def) {
        m_resourceType = def;
    }
    
    /**
     * <p>getResourceType</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.ResourceType} object.
     */
    public ResourceType getResourceType() {
        return m_resourceType;
    }
    
    /**
     * <p>getCollectionAgent</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     */
    public final CollectionAgent getCollectionAgent() {
        return m_resourceType.getAgent();
    }

    /** {@inheritDoc} */
    @Override
    public abstract boolean shouldPersist(ServiceParameters params);

    /**
     * <p>getOwnerName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getOwnerName() {
        return getCollectionAgent().getHostAddress();
    }

    /**
     * Returns ifType; is (but not sure if it should be) -1 for non interface type collections, otherwise
     * the SNMP type of the interface. This field is used to match the ifType field of the group from 
     * datacollection-config.xml.
     *
     * @return a int.
     */
    public abstract int getSnmpIfType();
    
    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean rescanNeeded() {
    	return false;
    }
    
    /**
     * <p>setAttributeValue</p>
     *
     * @param type a {@link org.opennms.netmgt.collectd.SnmpAttributeType} object.
     * @param val a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public void setAttributeValue(final SnmpAttributeType type, final SnmpValue val) {
        SnmpAttribute attr = new SnmpAttribute(this, type, val);
        addAttribute(attr);
    }

    private void addAttribute(final SnmpAttribute attr) {
        AttributeGroup group = getGroup(attr.getAttributeType().getGroupType());
        LOG.debug("Adding attribute {}: {} to group {}", attr.getClass().getName(), attr, group);
        group.addAttribute(attr);
    }

    private AttributeGroup getGroup(final AttributeGroupType groupType) {
        AttributeGroup group = m_groups.computeIfAbsent(groupType, g -> {
            return new SnmpAttributeGroup(this, g);
        });
        return group;
    }

    protected AttributeGroupType getGroupType(final String groupName) {
        for (AttributeGroupType type : m_groups.keySet()) {
            if (type.getName().equals(groupName)) {
                return type;
            }
        }
        return null;
    }
    /** {@inheritDoc} */
    @Override
    public void visit(final CollectionSetVisitor visitor) {
        visitor.visitResource(this);
        
        for (AttributeGroup group : getGroups()) {
            group.visit(visitor);
        }
        
        visitor.completeResource(this);
    }

    /**
     * <p>getGroups</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    protected Collection<AttributeGroup> getGroups() {
        return m_groups.values();
    }

    public List<CollectionAttribute> getStringAttributes() {
        return m_groups.values().stream()
        .flatMap(g -> g.getAttributes().stream())
        .filter(a -> a.getAttributeType() instanceof NumericCollectionAttributeType == false || AttributeType.STRING.equals(a.getAttributeType().getType()))
        .collect(Collectors.toList());
    }

    @Override
    public TimeKeeper getTimeKeeper() {
        return null;
    }

}
