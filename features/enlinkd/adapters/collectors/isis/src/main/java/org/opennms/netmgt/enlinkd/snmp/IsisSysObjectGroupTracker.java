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
package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.ErrorStatus;
import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsisSysObjectGroupTracker extends AggregateTracker {
    
    
    private static final Logger LOG = LoggerFactory.getLogger(IsisSysObjectGroupTracker.class);

    public final static String ISIS_SYS_ID_ALIAS = "isisSysID";
    public final static String ISIS_SYS_ID_OID = ".1.3.6.1.2.1.138.1.1.1.3";
    
    public final static String ISIS_SYS_ADMIN_STATE_ALIAS    = "isisSysAdminState";
    public final static String ISIS_SYS_ADMIN_STATE_OID = ".1.3.6.1.2.1.138.1.1.1.8";
    
    
    public static NamedSnmpVar[] ms_elemList;
    
    static {
        ms_elemList = new NamedSnmpVar[2];
        int ndx = 0;

        /*
         *   isisSysID OBJECT-TYPE
         *      SYNTAX IsisSystemID
         *        MAX-ACCESS read-create
         *        STATUS current
         *        DESCRIPTION
         *            "The ID for this Intermediate System.
         *             This value is appended to each of the
         *             area addresses to form the Network Entity Titles.
         *             The derivation of a value for this object is
         *             implementation specific.  Some implementations may
         *             automatically assign values and not permit an
         *             SNMP write, while others may require the value
         *             to be set manually.
         *
         *             Configured values MUST survive an agent reboot."
         *        REFERENCE "{ISIS.aoi systemId (119)}"
         *    ::= { isisSysObject 3 }
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,ISIS_SYS_ID_ALIAS,ISIS_SYS_ID_OID);
        
        /*
         * isisSysAdminState OBJECT-TYPE
         *        SYNTAX IsisAdminState
         *        MAX-ACCESS read-create
         *        STATUS current
         *        DESCRIPTION
         *            "The administrative state of this Intermediate
         *             System.  Setting this object to the value 'on'
         *             when its current value is 'off' enables
         *             the Intermediate System.
         *
         *             Configured values MUST survive an agent reboot."
         *        DEFVAL { off }
         *    ::= { isisSysObject 8 } 
         */
        ms_elemList[ndx] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,ISIS_SYS_ADMIN_STATE_ALIAS,ISIS_SYS_ADMIN_STATE_OID);
    }

    private final SnmpStore m_store;
    
    public IsisSysObjectGroupTracker() {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_store = new SnmpStore(ms_elemList);
    }
    
    public Integer getIsisSysAdminState() {
        return m_store.getInt32(ISIS_SYS_ADMIN_STATE_ALIAS);
        
    }
    
    public String getIsisSysId() {
        return m_store.getHexString(ISIS_SYS_ID_ALIAS);
    }
    
    
    /** {@inheritDoc} */
    @Override
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    @Override
    protected void reportGenErr(String msg) {
        LOG.warn("Error retrieving isisSysObject: {}", msg);
    }

    /** {@inheritDoc} */
    @Override
    protected void reportNoSuchNameErr(String msg) {
        LOG.info("Error retrieving isisSysObject: {}", msg);
    }

    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("Error retrieving isisSysObject: {}", ex.getMessage(), ex);
    }

    @Override
    protected void reportNonFatalErr(final ErrorStatus status) {
        LOG.info("Non-fatal error ({}) retrieving isisSysObject: {}", status, status.retry()? "Retrying." : "Giving up.");
    }

    public IsIsElement getIsisElement() {
    	IsIsElement element = new IsIsElement();
    	element.setIsisSysID(getIsisSysId());
    	element.setIsisSysAdminState(IsisAdminState.get(getIsisSysAdminState()));
    	return element;
    }

    @Override
    public void printSnmpData() {
        System.out.printf("\t\t%s (%s)= %s \n", ISIS_SYS_ID_OID, ISIS_SYS_ID_ALIAS, getIsisSysId());
        System.out.printf("\t\t%s (%s)= %s (%s)\n", ISIS_SYS_ADMIN_STATE_OID, ISIS_SYS_ADMIN_STATE_ALIAS, getIsisSysAdminState(), IsisAdminState.get(getIsisSysAdminState()));
    }

}
