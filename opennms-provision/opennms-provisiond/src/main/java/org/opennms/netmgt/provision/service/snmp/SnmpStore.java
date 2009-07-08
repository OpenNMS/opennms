/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


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
    
    protected Category log() {
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
