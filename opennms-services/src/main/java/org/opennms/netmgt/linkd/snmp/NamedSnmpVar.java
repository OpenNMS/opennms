/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd.snmp;

import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.ColumnTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The NamedSnmpVar class is used to associate a name for a particular snmp
 * instance with its object identifier. Common names often include ifIndex,
 * sysObjectId, etc al. These names are the names of particular variables as
 * defined by the SMI.
 *
 * Should the instance also be part of a table, then the column number of the
 * instance is also stored in the object.
 */
public final class NamedSnmpVar implements Collectable {
    /**
     * String which contains the Class name of the expected SNMP data type for
     * the object.
     */
    private final String m_type;

    /**
     * The class object for the class name stored in the m_type string.
     */
    private Class<?> m_typeClass;

    /**
     * The alias name for the object identifier.
     */
    private final String m_name;

    /**
     * The actual object identifer string for the object.
     */
    private final String m_oid;

    /**
     * If set then the object identifier is an entry some SNMP table.
     */
    private final boolean m_isTabular;

    /**
     * If the instance is part of a table then this is the column number for the
     * element.
     */
    private final int m_column;

    //
    // Class strings for valid SNMP data types
    // 
    /** Constant <code>SNMPINT32="org.opennms.protocols.snmp.SnmpInt32"</code> */
    public static final String SNMPINT32 = "org.opennms.protocols.snmp.SnmpInt32";

    /** Constant <code>SNMPUINT32="org.opennms.protocols.snmp.SnmpUInt32"</code> */
    public static final String SNMPUINT32 = "org.opennms.protocols.snmp.SnmpUInt32";

    /** Constant <code>SNMPCOUNTER32="org.opennms.protocols.snmp.SnmpCounter3"{trunked}</code> */
    public static final String SNMPCOUNTER32 = "org.opennms.protocols.snmp.SnmpCounter32";

    /** Constant <code>SNMPCOUNTER64="org.opennms.protocols.snmp.SnmpCounter6"{trunked}</code> */
    public static final String SNMPCOUNTER64 = "org.opennms.protocols.snmp.SnmpCounter64";

    /** Constant <code>SNMPGAUGE32="org.opennms.protocols.snmp.SnmpGauge32"</code> */
    public static final String SNMPGAUGE32 = "org.opennms.protocols.snmp.SnmpGauge32";

    /** Constant <code>SNMPTIMETICKS="org.opennms.protocols.snmp.SnmpTimeTick"{trunked}</code> */
    public static final String SNMPTIMETICKS = "org.opennms.protocols.snmp.SnmpTimeTicks";

    /** Constant <code>SNMPOCTETSTRING="org.opennms.protocols.snmp.SnmpOctetStr"{trunked}</code> */
    public static final String SNMPOCTETSTRING = "org.opennms.protocols.snmp.SnmpOctetString";

    /** Constant <code>SNMPOPAQUE="org.opennms.protocols.snmp.SnmpOpaque"</code> */
    public static final String SNMPOPAQUE = "org.opennms.protocols.snmp.SnmpOpaque";

    /** Constant <code>SNMPIPADDRESS="org.opennms.protocols.snmp.SnmpIPAddres"{trunked}</code> */
    public static final String SNMPIPADDRESS = "org.opennms.protocols.snmp.SnmpIPAddress";

    /** Constant <code>SNMPOBJECTID="org.opennms.protocols.snmp.SnmpObjectId"</code> */
    public static final String SNMPOBJECTID = "org.opennms.protocols.snmp.SnmpObjectId";

    /** Constant <code>SNMPV2PARTYCLOCK="org.opennms.protocols.snmp.SnmpV2PartyC"{trunked}</code> */
    public static final String SNMPV2PARTYCLOCK = "org.opennms.protocols.snmp.SnmpV2PartyClock";

    /** Constant <code>SNMPNOSUCHINSTANCE="org.opennms.protocols.snmp.SnmpNoSuchIn"{trunked}</code> */
    public static final String SNMPNOSUCHINSTANCE = "org.opennms.protocols.snmp.SnmpNoSuchInstance";

    /** Constant <code>SNMPNOSUCHOBJECT="org.opennms.protocols.snmp.SnmpNoSuchOb"{trunked}</code> */
    public static final String SNMPNOSUCHOBJECT = "org.opennms.protocols.snmp.SnmpNoSuchObject";

    /** Constant <code>SNMPENDOFMIBVIEW="org.opennms.protocols.snmp.SnmpEndOfMib"{trunked}</code> */
    public static final String SNMPENDOFMIBVIEW = "org.opennms.protocols.snmp.SnmpEndOfMibView";

    /** Constant <code>SNMPNULL="org.opennms.protocols.snmp.SnmpNull"</code> */
    public static final String SNMPNULL = "org.opennms.protocols.snmp.SnmpNull";

    /**
     * This constructor creates a new instance of the class with the type, alias
     * and object identifier. The instance is not considered to be part of a
     * table.
     *
     * @param type
     *            The expected SNMP data type of this object.
     * @param alias
     *            The alias for the object identifier.
     * @param oid
     *            The object identifier for the instance.
     */
    public NamedSnmpVar(final String type, final String alias, final String oid) {
        m_type = type;
        m_typeClass = null;
        m_name = alias;
        m_oid = oid;
        m_isTabular = false;
        m_column = 0;
    }

    /**
     * This constructor creates a new instance of the class with the type,
     * alias, object identifier, and table column set. The instance is
     * considered to be part of a table and the column is the "instance" number
     * for the table.
     *
     * @param type
     *            The expected SNMP data type of this object.
     * @param alias
     *            The alias for the object identifier.
     * @param oid
     *            The object identifier for the instance.
     * @param column
     *            The column entry for its table.
     */
    public NamedSnmpVar(final String type, final String alias, final String oid, final int column) {
        m_type = type;
        m_typeClass = null;
        m_name = alias;
        m_oid = oid;
        m_isTabular = true;
        m_column = column;
    }

    /**
     * Returns the class name stored in m_type which represents the expected
     * SNMP data type of the object.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getType() {
        return m_type;
    }

    /**
     * Returns the class object associated with the class name stored in m_type.
     *
     * @exception java.lang.ClassNotFoundException
     *                Thrown from this method if forName() fails.
     * @return a {@link java.lang.Class} object.
     * @throws java.lang.ClassNotFoundException if any.
     */
    public Class<?> getTypeClass() throws ClassNotFoundException {
        if (m_typeClass == null) {
            m_typeClass = Class.forName(m_type);
        }
        return m_typeClass;
    }

    /**
     * Returns the alias for the object identifier.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAlias() {
        return m_name;
    }

    /**
     * Returns the object identifer for this instance.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getOid() {
        return m_oid;
    }
    
    /**
     * <p>getSnmpObjId</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     */
    public SnmpObjId getSnmpObjId() {
        return SnmpObjId.get(m_oid);
    }

    /**
     * Returns true if this instance is part of a table.
     *
     * @return a boolean.
     */
    public boolean isTableEntry() {
        return m_isTabular;
    }
    
    /**
     * <p>getCollectionTracker</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.CollectionTracker} object.
     */
    @Override
    public CollectionTracker getCollectionTracker() {
        return m_isTabular ? (CollectionTracker)new ColumnTracker(getSnmpObjId()) : 
                             (CollectionTracker)new SingleInstanceTracker(getSnmpObjId(), SnmpInstId.INST_ZERO);
    }

    /**
     * Returns the column of the table this instance is in. If the instance is
     * not part of a table then the return code is not defined.
     *
     * @return a int.
     */
    public int getColumn() {
        return m_column;
    }

    /**
     * <p>getTrackersFor</p>
     *
     * @param columns an array of {@link org.opennms.netmgt.linkd.snmp.NamedSnmpVar} objects.
     * @return an array of {@link org.opennms.netmgt.snmp.CollectionTracker} objects.
     */
    public static CollectionTracker[] getTrackersFor(final NamedSnmpVar[] columns) {
        if (columns == null) {
            return new CollectionTracker[0];
        }
        CollectionTracker[] trackers = new CollectionTracker[columns.length];
        for(int i = 0; i < columns.length; i++)
            trackers[i] = columns[i].getCollectionTracker();
        
         return trackers;
    }

}
