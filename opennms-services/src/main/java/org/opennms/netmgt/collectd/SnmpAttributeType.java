/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an OID to be collected (it might be specific or an indexed object).
 * Also specific to a collection (e.g.: "default"), resource type (e.g.: node or
 * interface), and attribute group (data collection group name, e.g.: "mib2-interfaces").
 * This is extended to create concrete classes that represent specific types of data
 * to be stored such as numeric data ({@link (NumericAttributeType)}) or string data
 * ({@link (StringAttributeType)}).
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class SnmpAttributeType extends AbstractCollectionAttributeType {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpAttributeType.class);
    
    protected final MibObject m_mibObj;
    private final String m_collectionName;
    private final ResourceType m_resourceType;

    /**
     * <p>Constructor for SnmpAttributeType.</p>
     *
     * @param resourceType a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param collectionName a {@link java.lang.String} object.
     * @param mibObj a {@link org.opennms.netmgt.config.MibObject} object.
     * @param groupType a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
     */
    protected SnmpAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        super(groupType);
        m_resourceType = resourceType;
        m_collectionName = collectionName;
        m_mibObj = mibObj;
    }

    /**
     * <p>getCollectionName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected String getCollectionName() { return m_collectionName; }

    // FIXME: CollectionAttribute should be a tracker of its own
    // Also these should be created directly by the DAO rather 
    // than MibObject.
    /**
     * <p>getCollectionTrackers</p>
     *
     * @param objList a {@link java.util.Collection} object.
     * @return a {@link java.util.List} object.
     */
    public static List<Collectable> getCollectionTrackers(Collection<SnmpAttributeType> objList) {
        ArrayList<Collectable> trackers = new ArrayList<Collectable>(objList.size());
        for (SnmpAttributeType attrType : objList) {
            trackers.add(attrType.getCollectionTracker());
        }
        
        return trackers;
    }
    
    private CollectionTracker getCollectionTracker() {
        SnmpInstId[] instances = m_resourceType.getCollectionInstances();
        if (instances != null && Boolean.getBoolean("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances")) {
            return m_mibObj.getCollectionTracker(instances);
        } else {
            return m_mibObj.getCollectionTracker();
        }
    }
    

    /**
     * <p>create</p>
     *
     * @param resourceType a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param collectionName a {@link java.lang.String} object.
     * @param mibObj a {@link org.opennms.netmgt.config.MibObject} object.
     * @param groupType a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
     * @return a {@link org.opennms.netmgt.collectd.SnmpAttributeType} object.
     */
    public static SnmpAttributeType create(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        if (ResourceTypeUtils.isNumericType(mibObj.getType())) {
            return new NumericAttributeType(resourceType, collectionName, mibObj, groupType);
        }
        if (StringAttributeType.supportsType(mibObj.getType())) {
            return new StringAttributeType(resourceType, collectionName, mibObj, groupType);
        }
        
        throw new IllegalArgumentException("No support exists for AttributeType '" + mibObj.getType() + "' for MIB object: "+ mibObj);
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
     * <p>getGroupName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroupName() {
        return getGroupType().getName();
    }
    

    /**
     * <p>getAlias</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAlias() {
        return m_mibObj.getAlias();
    }
    
    /**
     * <p>getOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOid() {
        return m_mibObj.getOid();
    }

    /**
     * <p>getInstance</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstance() {
        return m_mibObj.getInstance();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AttributeDefinition#getType()
     */
    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getType() {
        return m_mibObj.getType();
    }

    SnmpObjId getSnmpObjId() {
        return m_mibObj.getSnmpObjId();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.AttributeDefinition#getName()
     */
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return getAlias();
    }

    /** {@inheritDoc} */
    @Override
    public abstract void storeAttribute(CollectionAttribute attribute, Persister persister);
    
    /**
     * <p>storeResult</p>
     *
     * @param collectionSet a {@link org.opennms.netmgt.collectd.SnmpCollectionSet} object.
     * @param entry a {@link org.opennms.netmgt.collectd.SNMPCollectorEntry} object.
     * @param res a {@link org.opennms.netmgt.snmp.SnmpResult} object.
     */
    public void storeResult(SnmpCollectionSet collectionSet, SNMPCollectorEntry entry, SnmpResult res) {
        LOG.debug("Setting attribute: {}.[{}] = '{}'", this, res.getInstance(), res.getValue());
        SnmpCollectionResource resource = null;
        if(this.getAlias().equals("ifAlias")) {
            resource = m_resourceType.findAliasedResource(res.getInstance(), res.getValue().toString());
        } else {
            resource = m_resourceType.findResource(res.getInstance());
        }
        if (resource == null) {
            collectionSet.notifyIfNotFound(this, res);
        } else {
            resource.setAttributeValue(this, res.getValue());
        }
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getAlias()+" ["+getOid()+"]";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SnmpAttributeType) {
            SnmpAttributeType attrType = (SnmpAttributeType) obj;
            return attrType.m_resourceType.equals(m_resourceType) && attrType.getAlias().equals(getAlias());
        }
        return false;
    }

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() {
        return getAlias().hashCode();
    }
    
    /**
     * <p>getGroupIfType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroupIfType() {
        return getGroupType().getIfType();
    }

    /**
     * <p>matches</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     * @return a boolean.
     */
    public boolean matches(SnmpObjId base, SnmpInstId inst) {
        if (!base.equals(getSnmpObjId())) {
        	return false;
        }
        
        if (getInstance().equals(MibObject.INSTANCE_IFINDEX) || m_mibObj.getResourceType() != null) {
            return true;
        } else { 
            return getInstance().equals(inst.toString());
        }
    }

}
