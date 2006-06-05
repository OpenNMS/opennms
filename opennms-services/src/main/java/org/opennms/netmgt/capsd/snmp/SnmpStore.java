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


import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.AbstractSnmpStore;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public class SnmpStore extends AbstractSnmpStore {
    
    /**
     * <P>
     * The keys that will be supported by default from the TreeMap base class.
     * Each of the elements in the list are an instance of the SNMP Interface
     * table. Objects in this list should be used by multiple instances of this
     * class.
     * </P>
     */
    protected NamedSnmpVar[] ms_elemList = null;

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
    
    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
        putValue(base.toString(), val);
        for(int i = 0; i < ms_elemList.length; i++) {
            NamedSnmpVar var = ms_elemList[i];
            if (base.equals(var.getSnmpObjId())) {
                log().debug("Storing Result: alias: "+var.getAlias()+" ["+base+"].["+inst+"] = "+toLogString(val));
                putValue(var.getAlias(), val);
            }
        }
    }
    
    private String toLogString(SnmpValue val) {
        if (val.getType() == SnmpValue.SNMP_OCTET_STRING) {
            return val.toDisplayString() + " (" + val.toHexString() + ")";
        }
        return val.toString();
    }

}
