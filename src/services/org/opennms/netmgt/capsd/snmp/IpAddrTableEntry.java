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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpUInt32;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * <P>
 * The IpAddrTableEntry class is designed to hold all the MIB-II information for
 * one entry in the ipAddrTable. The table effectively contains a list of these
 * entries, each entry having information about one address. The entry contains
 * an IP Address, its netmask, interface binding, broadcast address, and maximum
 * packet reassembly size.
 * </P>
 * 
 * <P>
 * This object is used by the IpAddrTable to hold infomation single entries in
 * the table. See the IpAddrTable documentation form more information.
 * </P>
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A>Jon Whetzel </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * 
 * @see IpAddrTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 */
public final class IpAddrTableEntry {
    // Lookup strings for specific table entries
    //
    public final static String IP_ADDR_ENT_ADDR = "ipAdEntAddr";

    public final static String IP_ADDR_IF_INDEX = "ipAdEntIfIndex";

    public final static String IP_ADDR_ENT_NETMASK = "ipAdEntNetMask";

    public final static String IP_ADDR_ENT_BCASTADDR = "ipAdEntBcastAddr";

    /**
     * <P>
     * The keys that will be supported by default from the TreeMap base class.
     * Each of the elements in the list are an instance of the SNMP Interface
     * table. Objects in this list should be used by multiple instances of this
     * class.
     * </P>
     */
    private static NamedSnmpVar[] ms_elemList = null;

    /**
     * <P>
     * Initialize the element list for the class. This is class wide data, but
     * will be used by each instance.
     * </P>
     */
    static {
        // Array size has changed from 5 to 4...no longer going after
        // ipAdEntReasmMaxSize variable because we aren't currently using
        // it and not all agents implement it which causes the collection
        // of the ipAddrTable to fail
        IpAddrTableEntry.ms_elemList = (new NamedSnmpVar[4]);
        int ndx = 0;

        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS, IP_ADDR_ENT_ADDR, ".1.3.6.1.2.1.4.20.1.1", 1);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IP_ADDR_IF_INDEX, ".1.3.6.1.2.1.4.20.1.2", 2);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPIPADDRESS, IP_ADDR_ENT_NETMASK, ".1.3.6.1.2.1.4.20.1.3", 3);
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32, IP_ADDR_ENT_BCASTADDR, ".1.3.6.1.2.1.4.20.1.4", 4);
        // ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
        // IP_ADDR_ENT_REASM_MAXSIZE, ".1.3.6.1.2.1.4.20.1.5", 5);
    }

    /**
     * <P>
     * The TABLE_OID is the object identifier that represents the root of the IP
     * Address table in the MIB forest.
     * </P>
     */
    public static final String TABLE_OID = ".1.3.6.1.2.1.4.20.1"; // start of
                                                                    // table
                                                                    // (GETNEXT)

    /**
     * <P>
     * The SnmpObjectId that represents the root of the interface tree. It is
     * created when the class is initialized and contains the value of
     * TABLE_OID.
     * 
     * @see #TABLE_OID
     */
    public static final SnmpObjectId ROOT = new SnmpObjectId(TABLE_OID);

    private Map m_responseMap = new TreeMap();

    /**
     * <P>
     * Creates a default instance of the IP Address table entry map. The map
     * represents a singular instance of the address table. Each column in the
     * table for the loaded instance may be retreived either through its name or
     * object identifier.
     * </P>
     * 
     * <P>
     * The initial table is constructied with zero elements in the map.
     * </P>
     */
    public IpAddrTableEntry() {
        super();
    }

    /**
     * <P>
     * The class constructor used to initialize the object to its initial state.
     * Although the object's member variables can change after an instance is
     * created, this constructor will initialize all the variables as per their
     * named variable from the passed array of SNMP varbinds.
     * </P>
     * 
     * <P>
     * If the information in the object should not be modified then a <EM>final
     * </EM> modifier can be applied to the created object.
     * </P>
     * 
     * @param vars
     *            The array of variable bindings.
     */
    public IpAddrTableEntry(SnmpVarBind[] vars) {
        this();
        update(vars);
    }

    /**
     * <P>
     * This method is used to update the map with the current information from
     * the agent. The array of variables should be all the elements in the
     * address row.
     * </P>
     * 
     * </P>
     * This does not clear out any column in the actual row that does not have a
     * definition.
     * </P>
     * 
     * @param vars
     *            The variables in the interface row.
     * 
     */
    public void update(SnmpVarBind[] vars) {
        Category log = ThreadCategory.getInstance(getClass());

        //
        // iterate through the variable bindings
        // and set the members appropiately.
        //
        // Note: the creation of the snmp object id
        // is in the outer loop to limit the times a
        // new object is created.
        //
        for (int x = 0; x < getElements().length; x++) {
            SnmpObjectId id = new SnmpObjectId(getElements()[x].getOid());

            for (int y = 0; y < vars.length; y++) {
                if (id.isRootOf(vars[y].getName())) {
                    try {
                        //
                        // Retrieve the class object of the expected SNMP data
                        // type for this element
                        //
                        Class classObj = getElements()[x].getTypeClass();

                        //
                        // If the SnmpSyntax object matches the expected class
                        // then store it in the map. Else, store a null pointer
                        // in the map.
                        //
                        if (classObj == null || classObj.isInstance(vars[y].getValue())) {
                            if (log.isDebugEnabled()) {
                                log.debug("update: Types match!  SNMP Alias: " + getElements()[x].getAlias() + "  Vars[y]: " + vars[y].toString());
                            }
                            put(getElements()[x].getAlias(), vars[y].getValue());
                            put(getElements()[x].getOid(), vars[y].getValue());
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("update: variable '" + vars[y].toString() + "' does NOT match expected type '" + getElements()[x].getType() + "'");
                            }
                            put(getElements()[x].getAlias(), null);
                            put(getElements()[x].getOid(), null);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error("Failed to retreive SNMP type class for element: " + getElements()[x].getAlias(), e);
                    } catch (NullPointerException e) {
                        log.error("Invalid reference", e);
                    }
                }
            }
        }
    }

    /**
     * <P>
     * Returns the number of entries in the MIB-II ipAddrTable element list.
     * </P>
     */
    public static int getElementListSize() {
        return getElements().length;
    }
    
    public InetAddress getIpAdEntAddr() {
        return getIPAddress(IpAddrTableEntry.IP_ADDR_ENT_ADDR);
    }

    public Integer getIpAdEntIfIndex() {
        return getInt32(IpAddrTableEntry.IP_ADDR_IF_INDEX);
    }

    public InetAddress getIpAdEntNetMask() {
        return getIPAddress(IpAddrTableEntry.IP_ADDR_ENT_NETMASK);
    }
    
    public InetAddress getIpAdEntBcastAddr() {
        return getIPAddress(IpAddrTableEntry.IP_ADDR_ENT_BCASTADDR);
    }

    private InetAddress getIPAddress(String key) {
        return SnmpIPAddress.toInetAddress((SnmpIPAddress) get(key));
    }
    
    private Integer getInt32(String key) {
        return SnmpInt32.toInteger((SnmpInt32) get(key));
    }
    
    private Long getUInt32(String key) {
        return SnmpUInt32.toLong((SnmpUInt32) get(key));
    }
    
    private String getDisplayString(String key) {
        return SnmpOctetString.toDisplayString((SnmpOctetString) get(key));
    }

    private String getHexString(String key) {
        return SnmpOctetString.toHexString((SnmpOctetString) get(key));
    }

    private Object get(String key) {
        return m_responseMap.get(key);
    }
    
    private void put(String key, Object value) {
        m_responseMap.put(key, value);
    }

    public static void setElements(NamedSnmpVar[] ms_elemList) {
        IpAddrTableEntry.ms_elemList = ms_elemList;
    }

    public static NamedSnmpVar[] getElements() {
        return ms_elemList;
    }




}
