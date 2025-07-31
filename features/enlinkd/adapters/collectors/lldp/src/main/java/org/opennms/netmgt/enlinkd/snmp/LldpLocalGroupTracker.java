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

import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.snmp.ErrorStatus;
import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LldpLocalGroupTracker extends AggregateTracker {

    private final static Logger LOG = LoggerFactory.getLogger(LldpLocalGroupTracker.class);
	
    public final static String LLDP_LOC_CHASSISID_SUBTYPE_ALIAS = "lldpLocChassisIdSubtype";
    public final static String LLDP_LOC_CHASSISID_SUBTYPE_OID = ".1.0.8802.1.1.2.1.3.1";
    
    public final static String LLDP_LOC_CHASSISID_ALIAS    = "lldpLocChassisId";
    public final static String LLDP_LOC_CHASSISID_OID    = ".1.0.8802.1.1.2.1.3.2";
    
    public final static String LLDP_LOC_SYSNAME_ALIAS = "lldpLocSysName";
    public final static String LLDP_LOC_SYSNAME_OID = ".1.0.8802.1.1.2.1.3.3";
    
    public static NamedSnmpVar[] ms_elemList;
    
    static {
        ms_elemList = new NamedSnmpVar[3];
        int ndx = 0;

        /*
         * <P>
         * "The type of encoding used to identify the chassis
         * associated with the local system."
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,LLDP_LOC_CHASSISID_SUBTYPE_ALIAS,LLDP_LOC_CHASSISID_SUBTYPE_OID);

        /*
         * <P>
         *  "The string value used to identify the chassis component
         *   associated with the local system."
         *   </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,LLDP_LOC_CHASSISID_ALIAS,LLDP_LOC_CHASSISID_OID);
        
        /*
         * <P>
         * "The string value used to identify the system name of the
         * local system.  If the local agent supports IETF RFC 3418,
         * lldpLocSysName object should have the same value of sysName
         * object."
         *   </P>
         */
        ms_elemList[ndx] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,LLDP_LOC_SYSNAME_ALIAS,LLDP_LOC_SYSNAME_OID);
    }

    private final SnmpStore m_store;
    
    public LldpLocalGroupTracker() {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_store = new SnmpStore(ms_elemList);
    }
    
    public Integer getLldpLocChassisidSubType() {
        return m_store.getInt32(LLDP_LOC_CHASSISID_SUBTYPE_ALIAS);
    }
    
    public SnmpValue getLldpLocChassisid() {
    	return m_store.getValue(LLDP_LOC_CHASSISID_ALIAS);
    }
    
    public String getLldpLocSysname() {
        return m_store.getDisplayString(LLDP_LOC_SYSNAME_ALIAS);
    }
    
    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    protected void reportGenErr(String msg) {
        LOG.warn("Error retrieving LLDP local group: {}",msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(String msg) {
        LOG.info("Error retrieving LLDP local group: {}",msg);
    }

    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("Error retrieving LLDP local group: {}", ex.getMessage(), ex);
    }

    @Override
    protected void reportNonFatalErr(final ErrorStatus status) {
        LOG.info("Non-fatal error ({}) retrieving LLDP local group: {}", status, status.retry()? "Retrying." : "Giving up.");
    }

    public LldpElement getLldpElement() {
		LldpElement lldpElement = new LldpElement();
        lldpElement.setLldpChassisIdSubType(LldpChassisIdSubType.get(getLldpLocChassisidSubType()));
		lldpElement.setLldpChassisId(LldpSnmpUtils.decodeLldpChassisId(lldpElement.getLldpChassisIdSubType(),getLldpLocChassisid()));
		lldpElement.setLldpSysname(getLldpLocSysname());
		return lldpElement;
    }

    @Override
    public void printSnmpData() {
        System.out.printf("\t\t%s (%s)= %s (%s)\n", LLDP_LOC_CHASSISID_SUBTYPE_OID, LLDP_LOC_CHASSISID_SUBTYPE_ALIAS, getLldpLocChassisidSubType(), LldpChassisIdSubType.get(getLldpLocChassisidSubType()));
        System.out.printf("\t\t%s (%s)= %s \n", LLDP_LOC_CHASSISID_OID, LLDP_LOC_CHASSISID_ALIAS , LldpSnmpUtils.decodeLldpChassisId(LldpChassisIdSubType.get(getLldpLocChassisidSubType()), getLldpLocChassisid()));
        System.out.printf("\t\t%s (%s)= %s \n", LLDP_LOC_SYSNAME_OID, LLDP_LOC_SYSNAME_ALIAS, getLldpLocSysname());
    }
	
}
