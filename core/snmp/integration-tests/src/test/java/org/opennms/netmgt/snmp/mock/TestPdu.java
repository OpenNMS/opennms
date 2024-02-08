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

public abstract class TestPdu {

    private TestVarBindList m_varBindList;

    public TestPdu() {
        m_varBindList = new TestVarBindList();
    }

    public static ResponsePdu getResponse() {
        return new ResponsePdu();
    }

    public static GetPdu getGet() {
        return new GetPdu();
    }

    
    public static NextPdu getNext() {
        return new NextPdu();
    }

    public static BulkPdu getBulk() {
        return new BulkPdu();
    }
    
    public TestVarBindList getVarBinds() {
        return m_varBindList;
    }

    void setVarBinds(TestVarBindList varBinds) {
        m_varBindList = new TestVarBindList(varBinds);
    }

    public void addVarBind(SnmpObjId objId) {
        m_varBindList.addVarBind(objId);
    }

    public void addVarBind(String oid, int inst) {
        addVarBind(SnmpObjId.get(oid, ""+inst));
    }

    public int size() {
        return m_varBindList.size();
    }

    public TestVarBind getVarBindAt(int i) {
        return m_varBindList.getVarBindAt(i);
    }

    public void addVarBind(String oid) {
        addVarBind(SnmpObjId.get(oid));
    }

    public void addVarBind(TestVarBind newVarBind) {
        m_varBindList.add(newVarBind);
    }



}