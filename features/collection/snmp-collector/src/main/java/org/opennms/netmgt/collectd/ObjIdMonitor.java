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
package org.opennms.netmgt.collectd;

import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <p>ObjIdMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class ObjIdMonitor extends SingleInstanceTracker {
    SnmpValue value;

    /**
     * <p>Constructor for ObjIdMonitor.</p>
     *
     * @param base a {@link org.opennms.netmgt.snmp.SnmpObjId} object.
     * @param inst a {@link org.opennms.netmgt.snmp.SnmpInstId} object.
     */
    public ObjIdMonitor(SnmpObjId base, SnmpInstId inst) {
        super(base, inst);
        value = null;
    }
    
    SnmpValue getValue() {
        return value;
    }
    
    int getIntValue() {
        return (value == null ? -1 : value.toInt());
    }
    
    long getLongValue() {
        return (value == null ? -1L : value.toLong());
    }

    /** {@inheritDoc} */
    @Override
    protected void storeResult(SnmpResult res) {
        value = res.getValue();
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        
        buffer.append(getClass().getName());
        buffer.append("@");
        buffer.append(Integer.toHexString(hashCode()));
        buffer.append(": value: " + getValue());
        
        return buffer.toString();
    }

}
