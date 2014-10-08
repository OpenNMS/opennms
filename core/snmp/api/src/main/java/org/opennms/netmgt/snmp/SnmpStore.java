/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnmpStore extends AbstractSnmpStore {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpStore.class);
    
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
     * @param list an array of {@link org.opennms.netmgt.snmp.NamedSnmpVar} objects.
     */
    public SnmpStore(final NamedSnmpVar[] list) {
        super();
        ms_elemList = list.clone();
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
     * @return an array of {@link org.opennms.netmgt.snmp.NamedSnmpVar} objects.
     */
    public NamedSnmpVar[] getElements() {
        return ms_elemList;
    }
    
    /** {@inheritDoc} */
    @Override
    public void storeResult(final SnmpResult res) {
        final SnmpObjId base = res.getBase();
        final SnmpValue value = res.getValue();

        putValue(base.toString(), value);

        for (final NamedSnmpVar var : ms_elemList) {
            if (base.equals(var.getSnmpObjId())) {
                if (value.isError()) {
                    LOG.error("storeResult: got an error for alias {} [{}].[{}], but we should only be getting non-errors: {}", var.getAlias(), base, res.getInstance(), value);
                } else if (value.isEndOfMib()) {
                    LOG.debug("storeResult: got endOfMib for alias {} [{}].[{}], not storing", var.getAlias(), base, res.getInstance());
                } else {
                    final SnmpValueType type = SnmpValueType.valueOf(value.getType());
                    LOG.debug("Storing Result: alias: {} [{}].[{}] = {}: {}", var.getAlias(), base, res.getInstance(), (type == null ? "Unknown" : type.getDisplayString()), toLogString(value));
                    putValue(var.getAlias(), value);
                }
            }
        }
    }
    
    private String toLogString(final SnmpValue val) {
        if (val.getType() == SnmpValue.SNMP_OCTET_STRING) {
            return val.toDisplayString() + " (" + val.toHexString() + ")";
        }
        return val.toString();
    }

}
