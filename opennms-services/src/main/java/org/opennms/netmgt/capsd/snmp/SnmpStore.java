/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.capsd.snmp;


import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.snmp.AbstractSnmpStore;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueType;

/**
 * <p>SnmpStore class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SnmpStore extends AbstractSnmpStore {
    
    /**
     * <P>
     * The keys that will be supported by default from the TreeMap base class.
     * Each of the elements in the list are an instance of the SNMP Interface
     * table. Objects in this list should be used by multiple instances of this
     * class.
     * </P>
     */
    protected final NamedSnmpVar[] ms_elemList;

    /**
     * <p>Constructor for SnmpStore.</p>
     *
     * @param list an array of {@link org.opennms.netmgt.capsd.snmp.NamedSnmpVar} objects.
     */
    public SnmpStore(NamedSnmpVar[] list) {
        super();
        ms_elemList = list;
    }

    /**
     * <P>
     * Returns the number of entries in the MIB-II ifTable element list.
     * </P>
     *
     * @return a int.
     */
    public int getElementListSize() {
        return ms_elemList.length;
    }

    /**
     * <p>getElements</p>
     *
     * @return an array of {@link org.opennms.netmgt.capsd.snmp.NamedSnmpVar} objects.
     */
    public NamedSnmpVar[] getElements() {
        return ms_elemList;
    }
    
    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /** {@inheritDoc} */
    @Override
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
