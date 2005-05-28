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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.collectd.mock;

import org.opennms.netmgt.collectd.SnmpObjId;

class TestPdu {
    int m_type;
    private TestVarBindList m_varBindList = new TestVarBindList();;
    private static final int RESPONSE = 0;
    public static final int GET_REQ = 1;
    public static final int NEXT_REQ = 2;
    public static final int BULK_REQ = 3;
    private int m_nonRepeaters;
    private int m_maxRepititions;
    public static final int NO_SUCH_NAME = 2;
    private int m_errorStatus;
    private int m_errorIndex;
    
    public TestPdu(int type) {
        m_type = type;
    }

    public static TestPdu getResponse() {
        return new TestPdu(RESPONSE);
    }

    public static TestPdu getGet() {
        return new TestPdu(GET_REQ);
    }

    
    public static TestPdu getNext() {
        return new TestPdu(NEXT_REQ);
    }

    public static TestPdu getBulk() {
        return new TestPdu(BULK_REQ);
    }

    public int getType() {
        return m_type;
    }

    public TestVarBindList getVarBinds() {
        return m_varBindList;
    }

    public void addVarBind(SnmpObjId objId, Object snmpData) {
        m_varBindList.addVarBind(objId, snmpData);
    }

    public void addVarBind(SnmpObjId objId) {
        m_varBindList.addVarBind(objId);
    }

    public void addVarBind(String oid, String inst) {
        addVarBind(SnmpObjId.get(oid, inst));
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

    public void setNonRepeaters(int nonRepeaters) {
        m_nonRepeaters = nonRepeaters;
    }
    
    public void setMaxRepititions(int maxRepititions) {
        m_maxRepititions = maxRepititions;
    }

    public int getNonRepeaters() {
        return m_nonRepeaters;
    }

    public int getMaxRepititions() {
        return m_maxRepititions;
    }

    public int getErrorStatus() {
        return m_errorStatus;
    }
    
    public void setErrorStatus(int errorStatus) {
        m_errorStatus = errorStatus;
    }
    
    public int getErrorIndex() {
        return m_errorIndex;
    }
    
    public void setErrorIndex(int errorIndex) {
        m_errorIndex = errorIndex;
    }

    
    
    
    
}