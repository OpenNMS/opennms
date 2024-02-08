/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
