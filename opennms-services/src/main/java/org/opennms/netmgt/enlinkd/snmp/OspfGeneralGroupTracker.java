/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

import java.net.InetAddress;

import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfElement.Status;
import org.opennms.netmgt.model.OspfElement.TruthValue;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.ErrorStatus;
import org.opennms.netmgt.snmp.ErrorStatusException;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OspfGeneralGroupTracker extends AggregateTracker {

	private final static Logger LOG = LoggerFactory.getLogger(OspfGeneralGroupTracker.class);
    public final static String OSPF_ROUTER_ID_ALIAS = "ospfRouterId";
    public final static String OSPF_ROUTER_ID_OID = ".1.3.6.1.2.1.14.1.1";

    public final static String OSPF_ADMIN_STAT_ALIAS = "ospfAdminStat";
    public final static String OSPF_ADMIN_STAT_OID = ".1.3.6.1.2.1.14.1.2";

    public final static String OSPF_VERSION_NUMBER_ALIAS = "ospfVersionNumber";
    public final static String OSPF_VERSION_NUMBER_OID = ".1.3.6.1.2.1.14.1.3";

    public final static String OSPF_AREA_BDR_RTR_STATUS_ALIAS = "ospfAreaBdrRtrStatus";
    public final static String OSPF_AREA_BDR_RTR_STATUS_OID = ".1.3.6.1.2.1.14.1.4";

    public final static String OSPF_AREA_AS_BDR_RTR_STATUS_ALIAS = "ospfAreaASBdrRtrStatus";
    public final static String OSPF_AREA_AS_BDR_RTR_STATUS_OID = ".1.3.6.1.2.1.14.1.5";
    

    public static NamedSnmpVar[] ms_elemList = null;
    
    static {
        ms_elemList = new NamedSnmpVar[5];
        int ndx = 0;

        /**
         * <P>
         * SYNTAX   RouterID
         * MAX-ACCESS   read-only
         * STATUS   current
         * DESCRIPTION
         * "A  32-bit  integer  uniquely  identifying  the
         * router in the Autonomous System.
         * 
         * By  convention,  to  ensure  uniqueness,   this
         * should  default  to  the  value  of  one of the
         * router's IP interface addresses."
         * REFERENCE
         * "OSPF Version 2, C.1 Global parameters"
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS,OSPF_ROUTER_ID_ALIAS,OSPF_ROUTER_ID_OID);
                
        /**
         * ospfAdminStat OBJECT-TYPE
         * SYNTAX   Status
         * MAX-ACCESS   read-only
         * STATUS   current
         * DESCRIPTION
         *   "The  administrative  status  of  OSPF  in  the
         *   router.   The  value 'enabled' denotes that the
         *    OSPF Process is active on at least  one  inter-
         *    face;  'disabled'  disables  it  on  all inter-
         *   faces."
         * ::= { ospfGeneralGroup 2 }        
         * 
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,OSPF_ADMIN_STAT_ALIAS,OSPF_ADMIN_STAT_OID);

        /**
         * ospfVersionNumber OBJECT-TYPE
         * SYNTAX   INTEGER    { version2 (2) }
         * MAX-ACCESS   read-only
         * STATUS   current
         * DESCRIPTION
         *    "The current version number of the OSPF  proto-
         *   col is 2."
         * REFERENCE
         *  "OSPF Version 2, Title"
         * ::= { ospfGeneralGroup 3 }
         * 
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,OSPF_VERSION_NUMBER_ALIAS,OSPF_VERSION_NUMBER_OID);
        
        /**
         * ospfAreaBdrRtrStatus OBJECT-TYPE
         * SYNTAX   TruthValue
         * MAX-ACCESS   read-only
         * STATUS   current
         * DESCRIPTION
         *    "A flag to note whether this router is an  area
         *    border router."
         * REFERENCE
         *   "OSPF Version 2, Section 3 Splitting the AS into
         *  Areas"
         * ::= { ospfGeneralGroup 4 }
         * 
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,OSPF_AREA_BDR_RTR_STATUS_ALIAS,OSPF_AREA_BDR_RTR_STATUS_OID);

        /**
         * ospfASBdrRtrStatus OBJECT-TYPE
         * SYNTAX   TruthValue
         * MAX-ACCESS   read-only
         * STATUS   current
         * DESCRIPTION
         *    "A flag to note whether this router is  config-
         *    ured as an Autonomous System border router."
         * REFERENCE
         *   "OSPF Version 2, Section 3.3  Classification  of
         *  routers"
         * ::= { ospfGeneralGroup 5 }
         * 
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,OSPF_AREA_AS_BDR_RTR_STATUS_ALIAS,OSPF_AREA_AS_BDR_RTR_STATUS_OID);
    }
    
    public static final String OSPF_GENERAL_GROUP_OID = ".1.3.6.1.2.1.14.1";

    private SnmpStore m_store;
    
    public OspfGeneralGroupTracker() {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_store = new SnmpStore(ms_elemList);
    }
    
    public InetAddress getOspfRouterId() {
        return m_store.getIPAddress(OSPF_ROUTER_ID_ALIAS);        
    }
     
    public Integer getOspfASBdrRtrStatus() {
    	return m_store.getInt32(OSPF_AREA_AS_BDR_RTR_STATUS_ALIAS);
    }

    public Integer getOspfBdrRtrStatus() {
    	return m_store.getInt32(OSPF_AREA_BDR_RTR_STATUS_ALIAS);
    }
    
    public Integer getOspfVersionNumber() {
    	return m_store.getInt32(OSPF_VERSION_NUMBER_ALIAS);
    }
    
    public Integer getOspfAdminStat() {
    	return m_store.getInt32(OSPF_ADMIN_STAT_ALIAS);
    }

    public OspfElement getOspfElement() {
    	OspfElement ospfElement = new OspfElement();
    	ospfElement.setOspfRouterId(getOspfRouterId());
    	ospfElement.setOspfAdminStat(Status.get(getOspfAdminStat()));
    	ospfElement.setOspfVersionNumber(getOspfVersionNumber());
    	ospfElement.setOspfBdrRtrStatus(TruthValue.get(getOspfBdrRtrStatus()));
    	ospfElement.setOspfASBdrRtrStatus(TruthValue.get(getOspfASBdrRtrStatus()));
    	return ospfElement;
    }
    
    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    protected void reportGenErr(String msg) {
        LOG.warn("Error retrieving OSPF general group: {}",msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(String msg) {
        LOG.info("Error retrieving OSPF general group: {}",msg);
    }

    @Override
    protected void reportFatalErr(final ErrorStatusException ex) {
        LOG.warn("Error retrieving OSPF general group: {}", ex.getMessage(), ex);
    }

    @Override
    protected void reportNonFatalErr(final ErrorStatus status) {
        LOG.info("Non-fatal error ({}) retrieving OSPF general group: {}", status, status.retry()? "Retrying." : "Giving up.");
    }

}
