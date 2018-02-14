/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.snmp.AbstractSnmpStore;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * The SNMPCollectorEntry class is designed to hold all SNMP collected data
 * pertaining to a particular interface.
 * </P>
 *
 * <P>
 * An instance of this class is created by calling the constructor and passing a
 * list of SnmpVarBindDTO objects from an SNMP PDU response. This class extends
 * java.util.TreeMap which is used to store each of the collected data points
 * indexed by object identifier.
 * </P>
 *
 * @author <A>Jon Whetzel </A>
 * @author <A>Jon Whetzel </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @version $Id: $
 */
public final class SNMPCollectorEntry extends AbstractSnmpStore {
    
    private static final Logger LOG = LoggerFactory.getLogger(SNMPCollectorEntry.class);
    
    /**
     * The list of MIBObjects that will used for associating the the data within
     * the map.
     */
    private Collection<SnmpAttributeType> m_attrList;
    private SnmpCollectionSet m_collectionSet;

    /**
     * <p>Constructor for SNMPCollectorEntry.</p>
     *
     * @param attrList a {@link java.util.Collection} object.
     * @param collectionSet a {@link org.opennms.netmgt.collectd.SnmpCollectionSet} object.
     */
    public SNMPCollectorEntry(Collection<SnmpAttributeType> attrList, SnmpCollectionSet collectionSet) {
        super();
        if (attrList == null) {
            throw new NullPointerException("attrList is null!");
        }
        m_attrList = attrList;
        m_collectionSet = collectionSet;
    }


    private List<SnmpAttributeType> findAttributeTypeForOid(SnmpObjId base, SnmpInstId inst) {
        List<SnmpAttributeType> matching = new LinkedList<>();
        for (SnmpAttributeType attrType : m_attrList) {
            if (attrType.matches(base, inst)) {
                matching.add(attrType);
            }
        }
        return matching;
    }


    /** {@inheritDoc} */
    @Override
    public void storeResult(SnmpResult res) {
        String key = res.getAbsoluteInstance().toString();
        putValue(key, res.getValue());
        List<SnmpAttributeType> attrTypes = findAttributeTypeForOid(res.getBase(), res.getInstance());
        if (attrTypes.isEmpty()) {
        	throw new IllegalArgumentException("Received result for unexpected oid ["+res.getBase()+"].["+res.getInstance()+"]");
        }
        
        for (SnmpAttributeType attrType : attrTypes) {
            if (attrType.getInstance().equals(MibObject.INSTANCE_IFINDEX)) {
                putIfIndex(res.getInstance().toInt());
            }
            attrType.storeResult(m_collectionSet, this, res);
            LOG.debug("storeResult: added value for {}: {}", attrType.getAlias(), res.toString());
        }
    }


    String getValueForBase(String baseOid) {
    
        String instance = String.valueOf(getIfIndex()); 
        if (instance == null || instance.equals("")) {
            return null;
        }
        
    
        String fullOid = baseOid + "." + instance;
    
        String snmpVar = getDisplayString(fullOid);
        if (snmpVar == null) {
            return null;
        }
    
        snmpVar = snmpVar.trim();
    
        if (snmpVar.equals("")) {
            return null;
        }
    
        return snmpVar;
    
    }
}
