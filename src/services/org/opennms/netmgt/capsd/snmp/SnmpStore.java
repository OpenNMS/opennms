//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.capsd.snmp;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpUInt32;
import org.opennms.protocols.snmp.SnmpVarBind;

public class SnmpStore {

    private Map m_responseMap = new TreeMap();
    /**
     * <P>
     * The keys that will be supported by default from the TreeMap base class.
     * Each of the elements in the list are an instance of the SNMP Interface
     * table. Objects in this list should be used by multiple instances of this
     * class.
     * </P>
     */
    protected NamedSnmpVar[] ms_elemList = null;

    protected static Category log() {
        return ThreadCategory.getInstance(SnmpTableEntry.class);
    }

    public SnmpStore(NamedSnmpVar[] list) {
        super();
        ms_elemList = list;
    }

    /**
     * <P>
     * Returns the number of entries in the MIB-II ifTable element list.
     * </P>
     */
    public int getElementListSize() {
        return ms_elemList.length;
    }

    public NamedSnmpVar[] getElements() {
        return ms_elemList;
    }

    public Integer getInt32(String key) {
        return SnmpInt32.toInteger((SnmpInt32) get(key));
    }

    public Long getUInt32(String key) {
        return SnmpUInt32.toLong((SnmpUInt32) get(key));
    }

    public String getDisplayString(String key) {
        return SnmpOctetString.toDisplayString((SnmpOctetString) get(key));
    }

    public String getHexString(String key) {
        return SnmpOctetString.toHexString((SnmpOctetString) get(key));
    }

    public String getObjectID(String sys_objectid) {
        return (get(SystemGroup.SYS_OBJECTID) == null ? null : get(SystemGroup.SYS_OBJECTID).toString());
    }

    protected Object get(String key) {
        return m_responseMap.get(key);
    }

    protected void put(String key, Object value) {
        m_responseMap.put(key, value);
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
        for (int col = 0; col < getElements().length; col++) {
            SnmpObjectId id = new SnmpObjectId(getElements()[col].getOid());
    
            for (int varBind = 0; varBind < vars.length; varBind++) {
                if (id.isRootOf(vars[varBind].getName())) {
                    try {
                        //
                        // Retrieve the class object of the expected SNMP data
                        // type for this element
                        //
                        Class classObj = getElements()[col].getTypeClass();
    
                        //
                        // If the SnmpSyntax object matches the expected class
                        // then store it in the map. Else, store a null pointer
                        // in the map.
                        //
                        if (classObj == null || classObj.isInstance(vars[varBind].getValue())) {
                            if (log.isDebugEnabled()) {
                                log.debug("update: Types match!  SNMP Alias: " + getElements()[col].getAlias() + "  Vars[y]: " + vars[varBind].toString());
                            }
                            put(getElements()[col].getAlias(), vars[varBind].getValue());
                            put(getElements()[col].getOid(), vars[varBind].getValue());
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("update: variable '" + vars[varBind].toString() + "' does NOT match expected type '" + getElements()[col].getType() + "'");
                            }
                            put(getElements()[col].getAlias(), null);
                            put(getElements()[col].getOid(), null);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error("Failed to retreive SNMP type class for element: " + getElements()[col].getAlias(), e);
                    } catch (NullPointerException e) {
                        log.error("Invalid reference", e);
                    }
                }
            }
        }
    }


}
