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
// Modifications:
//
// 2008 May 10: Don't store a value in storeResult if it is an error or
//              signals end of MIB.  Also display the value type in logs. - dj@opennms.org
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

package org.opennms.netmgt.provision.service.snmp;


import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.AbstractSnmpStore;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueType;

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
    
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void storeResult(SnmpResult res) {
        putValue(res.getBase().toString(), res.getValue());
        for (NamedSnmpVar var : ms_elemList) {
            if (res.getBase().equals(var.getSnmpObjId())) {
                if (res.getValue().isError()) {
                    log().error("storeResult: got an error for alias "+var.getAlias()+" ["+res.getBase()+"].["+res.getInstance()+"], but we should only be getting non-errors: " + res.getValue());
                } else if (res.getValue().isEndOfMib()) {
                    log().debug("storeResult: got endOfMib for alias "+var.getAlias()+" ["+res.getBase()+"].["+res.getInstance()+"], not storing");
                } else {
                    SnmpValueType type = SnmpValueType.valueOf(res.getValue().getType());
                    log().debug("Storing Result: alias: "+var.getAlias()+" ["+res.getBase()+"].["+res.getInstance()+"] = " + (type == null ? "Unknown" : type.getDisplayString()) + ": "+toLogString(res.getValue()));
                    putValue(var.getAlias(), res.getValue());
                }
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
