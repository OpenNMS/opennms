/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpElement.LldpChassisIdSubType;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LldpLocalGroupTracker extends AggregateTracker {

	private final static Logger LOG = LoggerFactory.getLogger(LldpLocalGroupTracker.class);
	
    public final static String LLDP_LOC_CHASSISID_SUBTYPE_ALIAS = "lldpLocChassisIdSubtype";
    public final static String LLDP_LOC_CHASSISID_SUBTYPE_OID = ".1.0.8802.1.1.2.1.3.1";
    
    public final static String LLDP_LOC_CHASSISID_ALIAS    = "lldpLocChassisId";
    public final static String LLDP_LOC_CHASSISID_OID    = ".1.0.8802.1.1.2.1.3.2";
    
    public final static String LLDP_LOC_SYSNAME_ALIAS = "lldpLocSysName";
    public final static String LLDP_LOC_SYSNAME_OID = ".1.0.8802.1.1.2.1.3.3";
    
    public static NamedSnmpVar[] ms_elemList = null;
    
    static {
        ms_elemList = new NamedSnmpVar[3];
        int ndx = 0;

        /**
         * <P>
         * "The type of encoding used to identify the chassis
         * associated with the local system."
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,LLDP_LOC_CHASSISID_SUBTYPE_ALIAS,LLDP_LOC_CHASSISID_SUBTYPE_OID);

        /**
         * <P>
         *  "The string value used to identify the chassis component
         *   associated with the local system."
         *   </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,LLDP_LOC_CHASSISID_ALIAS,LLDP_LOC_CHASSISID_OID);
        
        /**
         * <P>
         * "The string value used to identify the system name of the
         * local system.  If the local agent supports IETF RFC 3418,
         * lldpLocSysName object should have the same value of sysName
         * object."
         *   </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,LLDP_LOC_SYSNAME_ALIAS,LLDP_LOC_SYSNAME_OID);
    }
    
    public static final String LLDP_LOC_OID = ".1.0.8802.1.1.2.1.3";

    private SnmpStore m_store;
    
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
        LOG.warn("Error retrieving lldpLocalGroup: {}",msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(String msg) {
        LOG.info("Error retrieving lldpLocalGroup: {}",msg);
    }

	public LldpElement getLldpElement() {
		LldpElement lldpElement = new LldpElement();
		lldpElement.setLldpChassisId(LldpHelper.decodeLldpChassisId(getLldpLocChassisid(), getLldpLocChassisidSubType()));
		lldpElement.setLldpChassisIdSubType(LldpChassisIdSubType.get(getLldpLocChassisidSubType()));
		lldpElement.setLldpSysname(getLldpLocSysname());
		return lldpElement;
	}
	
}
