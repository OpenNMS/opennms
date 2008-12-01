//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//
// Tab Size = 8
//

package org.opennms.netmgt.provision.service.snmp;

import org.opennms.netmgt.snmp.AbstractSnmpStore;


/**
 * <P>
 * This object contains a list of all the elements defined in the MIB-II
 * interface table. An instance object is initialized by calling the constructor
 * and passing in a variable list from an SNMP PDU. The actual data can be
 * recovered via the base class map interface.
 * </P>
 * 
 * <P>
 * Once an instance is created and its data set either via the constructor or
 * from the update method, the actual elements can be retreived using the
 * instance names. The names include: <EM>ifIndex</EM>,<EM>ifDescr</EM>,
 * <EM>ifSpeed</EM>,<EM>etc al</EM>. The information can also be accessed
 * by using the complete object identifer for the entry.
 * </P>
 * 
 * <P>
 * For more information on the individual fields, and to find out their
 * respective object identifiers see RFC1213 from the IETF.
 * </P>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A>Jon Whetzel </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public final class IfTableEntry extends SnmpTableEntry {
    //
    // Lookup strings for specific table entries
    //
    public final static String IF_INDEX = AbstractSnmpStore.IFINDEX;

    public final static String IF_DESCR = "ifDescr";

    public final static String IF_TYPE = "ifType";

    public final static String IF_MTU = "ifMtu";

    public final static String IF_SPEED = "ifSpeed";

    public final static String IF_PHYS_ADDR = "ifPhysAddr";

    public final static String IF_ADMIN_STATUS = "ifAdminStatus";

    public final static String IF_OPER_STATUS = "ifOperStatus";

    public final static String IF_LAST_CHANGE = "ifLastChange";

    public final static String IF_IN_OCTETS = "ifInOctets";

    public final static String IF_IN_UCAST = "ifInUcastPkts";

    public final static String IF_IN_NUCAST = "ifInNUcastPkts";

    public final static String IF_IN_DISCARDS = "ifInDiscards";

    public final static String IF_IN_ERRORS = "ifInErrors";

    public final static String IF_IN_UKNOWN_PROTOS = "ifInUnknownProtos";

    public final static String IF_OUT_OCTETS = "ifOutOctets";

    public final static String IF_OUT_UCAST = "ifOutUcastPkts";

    public final static String IF_OUT_NUCAST = "ifOutNUcastPkts";

    public final static String IF_OUT_DISCARDS = "ifOutDiscards";

    public final static String IF_OUT_ERRORS = "ifOutErrors";

    public final static String IF_OUT_QLEN = "ifOutQLen";

    public final static String IF_SPECIFIC = "ifSpecific";
    
    public static NamedSnmpVar[] ms_elemList = null;

    /**
     * <P>
     * Initialize the element list for the class. This is class wide data, but
     * will be used by each instance.
     * </P>
     */
    static {
        ms_elemList = new NamedSnmpVar[9];
        int ndx = 0;

        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IF_INDEX, ".1.3.6.1.2.1.2.2.1.1", 1);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, IF_DESCR, ".1.3.6.1.2.1.2.2.1.2", 2);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IF_TYPE, ".1.3.6.1.2.1.2.2.1.3", 3);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IF_MTU, ".1.3.6.1.2.1.2.2.1.4", 4);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32, IF_SPEED, ".1.3.6.1.2.1.2.2.1.5", 5);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, IF_PHYS_ADDR, ".1.3.6.1.2.1.2.2.1.6", 6);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IF_ADMIN_STATUS, ".1.3.6.1.2.1.2.2.1.7", 7);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IF_OPER_STATUS, ".1.3.6.1.2.1.2.2.1.8", 8);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPTIMETICKS, IF_LAST_CHANGE, ".1.3.6.1.2.1.2.2.1.9", 9);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_IN_OCTETS, ".1.3.6.1.2.1.2.2.1.10", 10);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_IN_UCAST, ".1.3.6.1.2.1.2.2.1.11", 11);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_IN_NUCAST, ".1.3.6.1.2.1.2.2.1.12", 12);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_IN_DISCARDS, ".1.3.6.1.2.1.2.2.1.13", 13);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_IN_ERRORS, ".1.3.6.1.2.1.2.2.1.14", 14);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_IN_UKNOWN_PROTOS, ".1.3.6.1.2.1.2.2.1.15", 15);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_OUT_OCTETS, ".1.3.6.1.2.1.2.2.1.16", 16);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_OUT_UCAST, ".1.3.6.1.2.1.2.2.1.17", 17);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_OUT_NUCAST, ".1.3.6.1.2.1.2.2.1.18", 18);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_OUT_DISCARDS, ".1.3.6.1.2.1.2.2.1.19", 19);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
        // IF_OUT_ERRORS, ".1.3.6.1.2.1.2.2.1.20", 20);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPGAUGE32,
        // IF_OUT_QLEN, ".1.3.6.1.2.1.2.2.1.21", 21);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID,
        // IF_SPECIFIC, ".1.3.6.1.2.1.2.2.1.22", 22);
    }

    /**
     * <P>
     * The TABLE_OID is the object identifier that represents the root of the
     * interface table in the MIB forest.
     * </P>
     */
    public static final String TABLE_OID = ".1.3.6.1.2.1.2.2.1"; // start of
                                                                    // table
                                                                    // (GETNEXT)

    /**
     * <P>
     * The class constructor used to initialize the object to its initial state.
     * Although the object's attributes and data can be changed after its
     * created, this constructor will initialize all the variables as per their
     * named varbind in the passed array. This array should have been collected
     * from an SnmpPduRequest that was received from a remote host.
     * </P>
     * 
     * @param vars
     *            The array of variable bindings.
     * 
     */
    public IfTableEntry() {
        super(ms_elemList);
    }

    public Integer getIfType() {
        return getInt32(IfTableEntry.IF_TYPE);
    }
    
    public Integer getIfAdminStatus() {
        return getInt32(IfTableEntry.IF_ADMIN_STATUS);
    }

    public String getIfDescr() {
        return getDisplayString(IfTableEntry.IF_DESCR);
    }

    public String getPhysAddr() {
        return getHexString(IfTableEntry.IF_PHYS_ADDR);
    }

    public Integer getIfOperStatus() {
        return getInt32(IfTableEntry.IF_OPER_STATUS);
    }

    public Long getIfSpeed() {
        return getUInt32(IfTableEntry.IF_SPEED);
    }

}
