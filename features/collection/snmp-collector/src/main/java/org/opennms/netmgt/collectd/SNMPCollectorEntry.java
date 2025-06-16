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
