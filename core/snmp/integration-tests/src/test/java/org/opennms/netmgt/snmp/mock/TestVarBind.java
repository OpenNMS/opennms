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
package org.opennms.netmgt.snmp.mock;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;

public class TestVarBind {
    private SnmpObjId m_oid;
    private SnmpValue m_val;
    
    public TestVarBind(SnmpObjId oid) {
        this(oid, null);
    }
    
    public TestVarBind(SnmpObjId oid, SnmpValue val) {
        m_oid = oid;
        m_val = val;
        
    }
    public SnmpObjId getObjId() {
        return m_oid;
    }
    
    public SnmpValue getValue() {
        return m_val;
    }
}