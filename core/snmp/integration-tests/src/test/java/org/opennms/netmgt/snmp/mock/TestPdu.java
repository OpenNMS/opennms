/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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