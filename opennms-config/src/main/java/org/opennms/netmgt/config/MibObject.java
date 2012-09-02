/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.ColumnTracker;
import org.opennms.netmgt.snmp.InstanceListTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * This class is responsible for holding information about a particular MIB
 * object parsed from the DataCollection.xml file.
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 */
public class MibObject implements Collectable {
    /**
     * Object's identifier in dotted-decimal notation (e.g, ".1.3.6.1.2.1.1.1").
     */
    private String m_oid;
    
    private SnmpObjId m_snmpObjId = null;

    /**
     * Object's alias (e.g., "sysDescription").
     */
    private String m_alias;

    /**
     * Object's expected data type.
     */
    private String m_type;

    /**
     * Object's maximum value.
     */
    private String m_maxval;

    /**
     * Object's minimum value.
     */
    private String m_minval;

    /**
     * Object's instance to be retrieved. Indicates a value to be appended to
     * the objects' oid string prior to issuing an SNMP GET request.
     * 
     * decimal value: instance of object to retrieve (value is appended to the
     * objects oid string) keyword value: currently supported keywords are
     * "ifIndex" and "ifAddress".
     * 
     * @see #INSTANCE_IFINDEX
     * @see #INSTANCE_IFADDRESS
     */
    private String m_instance;

    private String m_groupName;

    private String m_groupIfType;

	private ResourceType m_resourceType;

    /**
     * Reserved instance keywords.
     */
    /**
     * Indicates that the interface's 'ifIndex' value from the 'ipInterface'
     * table of the database should be appended to the object's oid.
     */
    public static final String INSTANCE_IFINDEX = "ifIndex";

    /**
     * Indicates that the interface's IP address is to be appended to the
     * object's oid.
     */
    public static final String INSTANCE_IFADDRESS = "ifAddress";

    /**
     * Constructor
     */
    public MibObject() {
        m_oid = null;
        m_alias = null;
        m_type = null;
        m_instance = null;
        m_maxval = null;
        m_minval = null;
    }

    /**
     * This method is used to assign the object's identifier.
     *
     * @param oid -
     *            object identifier in dotted decimal notation (e.g.,
     *            ".1.3.6.1.2.1.1.1")
     */
    public void setOid(String oid) {
        m_oid = oid;
        m_snmpObjId = null;
    }

    /**
     * This method is used to assign the object's alias.
     *
     * @param alias -
     *            object alias (e.g., "sysDescription")
     */
    public void setAlias(String alias) {
        m_alias = alias;
    }

    /**
     * This method is used to assign the object's expected data type.
     *
     * @param type -
     *            object's data type
     */
    public void setType(String type) {
        m_type = type;
    }

    /**
     * This method is used to assign the object's maximum value.
     *
     * @param maxval
     *            object's maximum value
     */
    public void setMaxval(String maxval) {
        m_maxval = maxval;
    }

    /**
     * This method is used to assign the object's minimum value.
     *
     * @param minval
     *            object's minimum value
     */
    public void setMinval(String minval) {
        m_minval = minval;
    }

    /**
     * This method is used to specify the object's instance to be retrieved. The
     * instance specified here will be dereferenced if necessary and appended to
     * the object's identifier string. Valid instance values are keywords such
     * as "ifIndex" and "ifAddress" or numeric values such as "0" or "99".
     * Numeric values will simply be appended to the objects identifer as-is
     * while keyword values will be dereferenced and will be assigned a valued
     * which is dependent on the SNMP agent's IP address.
     *
     * @see #INSTANCE_IFINDEX
     * @see #INSTANCE_IFADDRESS
     * @see #INSTANCE_IFINDEX
     * @see #INSTANCE_IFADDRESS
     * @param instance a {@link java.lang.String} object.
     */
    public void setInstance(String instance) {
        m_instance = instance;
    }
    
    /**
     * <p>setGroupName</p>
     *
     * @param groupName a {@link java.lang.String} object.
     */
    public void setGroupName(String groupName) {
        m_groupName = groupName;
    }

    /**
     * Returns the object's identifier.
     *
     * @return The object's identifier string.
     */
    public String getOid() {
        return m_oid;
    }

    /**
     * Returns the object's maximum value.
     *
     * @return The object's maxval.
     */
    public String getMaxval() {
        // TODO: No one uses these... Do we need them?
        return m_maxval;
    }

    /**
     * Returns the object's minimum value.
     *
     * @return The object's minval.
     */
    public String getMinval() {
        // TODO: No one uses these... Do we need them?
        return m_minval;
    }

    /**
     * Returns the object's alias.
     *
     * @return The object's alias.
     */
    public String getAlias() {
        return m_alias;
    }

    /**
     * Returns the object's data type.
     *
     * @return The object's data type
     */
    public String getType() {
        return m_type;
    }

    /**
     * Returns the instance string associated with the object.
     *
     * @return The instance value associated with the object
     */
    public String getInstance() {
        return m_instance;
    }
    
    /**
     * <p>getGroupName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroupName() {
        return m_groupName;
    }

    /**
     * {@inheritDoc}
     *
     * This method is responsible for comparing this MibObject with the passed
     * Object to determine if they are equivalent. The objects are equivalent if
     * the argument is a MibObject object with the same object identifier,
     * instance, alias and type.
     */
    public boolean equals(Object object) {
        if (object == null)
            return false;

        MibObject aMibObject;

        try {
            aMibObject = (MibObject) object;
        } catch (ClassCastException cce) {
            return false;
        }

        if (m_oid.equals(aMibObject.getOid())) {
            if (m_instance.equals(aMibObject.getInstance())) {
                //
                // 'alias' and 'type', values are optional so we need to check
                // for null
                //
                if ((m_alias == null && aMibObject.getInstance() == null) || m_alias.equals(aMibObject.getAlias())) {
                    if ((m_type == null && aMibObject.getType() == null) || m_type.equals(aMibObject.getType())) {
                        return true;
                    }
                }
            }
        }

        return false;

    }
    
    

    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return m_oid.hashCode();
    }

    /**
     * This method is responsible for returning a String object which represents
     * the content of this MibObject. Primarily used for debugging purposes.
     *
     * @return String which represents the content of this MibObject
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        // Build the buffer
        buffer.append("\n   group:    ").append(m_groupName);
        buffer.append("\n   oid:      ").append(m_oid);
        buffer.append("\n   instance: ").append(m_instance);
        buffer.append("\n   alias:    ").append(m_alias);
        buffer.append("\n   type:     ").append(m_type);

        return buffer.toString();
    }
    
    /**
     * <p>getCollectionTracker</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public CollectionTracker getCollectionTracker() {
        if (INSTANCE_IFINDEX.equals(getInstance()) || getResourceType() != null) {
            return (CollectionTracker) new ColumnTracker(SnmpObjId.get(getOid()));
        } else {
            return (CollectionTracker) new InstanceListTracker(SnmpObjId.get(getOid()), getInstance());
            
        }
    }

    /**
     * <p>getCollectionTracker</p>
     *
     * @param instances a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     * @return a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    public CollectionTracker getCollectionTracker(SnmpInstId... instances) {
        if (INSTANCE_IFINDEX.equals(getInstance()) || getResourceType() != null) {
            return (CollectionTracker) new InstanceListTracker(SnmpObjId.get(getOid()), instances);
        } else {
            return (CollectionTracker) new InstanceListTracker(SnmpObjId.get(getOid()), getInstance());
            
        }
    }

    /**
     * <p>getCollectionTrackers</p>
     *
     * @param objList a {@link java.util.List} object.
     * @return an array of {@link org.opennms.netmgt.snmp.CollectionTracker} objects.
     */
    public static CollectionTracker[] getCollectionTrackers(List<MibObject> objList) {
        CollectionTracker[] trackers = new CollectionTracker[objList.size()];
        int index = 0;
        for (Iterator<MibObject> it = objList.iterator(); it.hasNext();) {
            MibObject mibObj = it.next();
            trackers[index++] = mibObj.getCollectionTracker();
        }
        return trackers;
    }

    /**
     * <p>getCollectionTrackers</p>
     *
     * @param objList a {@link java.util.List} object.
     * @param instances a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     * @return an array of {@link org.opennms.netmgt.snmp.CollectionTracker} objects.
     */
    public static CollectionTracker[] getCollectionTrackers(List<MibObject> objList, SnmpInstId... instances) {
        CollectionTracker[] trackers = new CollectionTracker[objList.size()];
        int index = 0;
        for (Iterator<MibObject> it = objList.iterator(); it.hasNext();) {
            MibObject mibObj = it.next();
            trackers[index++] = mibObj.getCollectionTracker(instances);
        }
        return trackers;
    }

    /**
     * <p>getSnmpObjId</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public SnmpObjId getSnmpObjId() {
        if (getOid() == null)
            return null;
        
        if (m_snmpObjId == null)
            m_snmpObjId = SnmpObjId.get(getOid());
        
        return m_snmpObjId;
    }

    /**
     * <p>setGroupIfType</p>
     *
     * @param groupIfType a {@link java.lang.String} object.
     */
    public void setGroupIfType(String groupIfType) {
        m_groupIfType = groupIfType;
    }
    
    /**
     * <p>getGroupIfType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroupIfType() {
        return m_groupIfType;
    }

	/**
	 * <p>setResourceType</p>
	 *
	 * @param resourceType a {@link org.opennms.netmgt.config.datacollection.ResourceType} object.
	 */
	public void setResourceType(ResourceType resourceType) {
		m_resourceType = resourceType;
	}
	
	/**
	 * <p>getResourceType</p>
	 *
	 * @return a {@link org.opennms.netmgt.config.datacollection.ResourceType} object.
	 */
	public ResourceType getResourceType() {
		return m_resourceType;
	}


}
