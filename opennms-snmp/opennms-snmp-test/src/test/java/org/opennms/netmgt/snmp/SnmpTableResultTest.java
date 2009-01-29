/*
 * This file is part of the OpenNMS(R) Application. OpenNMS(R) is Copyright
 * (C) 2009 The OpenNMS Group, Inc. All rights reserved. OpenNMS(R) is a
 * derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified and included code are below. OpenNMS(R) is a registered
 * trademark of The OpenNMS Group, Inc. Modifications: Original code base
 * Copyright (C) 1999-2001 Oculan Corp. All rights reserved. This program is
 * free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. For more information contact: OpenNMS Licensing
 * <license@opennms.org> http://www.opennms.org/ http://www.opennms.com/
 */

package org.opennms.netmgt.snmp;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author brozow
 */
public class SnmpTableResultTest {

    private final SnmpObjId m_ifTable = SnmpObjId.get(".1.3.6.1.2.1.2.2.1");
    private final SnmpObjId m_ifIndex = SnmpObjId.get(m_ifTable, "1");
    private final SnmpObjId m_ifDescr = SnmpObjId.get(m_ifTable, "2");
    private final SnmpObjId m_ifType = SnmpObjId.get(m_ifTable, "3");
    private final SnmpObjId m_ifMtu = SnmpObjId.get(m_ifTable, "4");

    private SnmpTableResult m_tableResult;
    private TestRowCallback m_rowCallback;
    private SnmpObjId[] m_columns;
    private List<SnmpRowResult> m_anticipatedRows = new ArrayList<SnmpRowResult>();
    private List<SnmpRowResult> m_receivedRows = new ArrayList<SnmpRowResult>();

    private class TestRowCallback implements RowCallback {
        private int m_rowCount = 0;

        public int getRowCount() {
            return m_rowCount;
        }

        /*
         * (non-Javadoc)
         * @see
         * org.opennms.netmgt.snmp.RowCallback#rowCompleted(org.opennms.netmgt
         * .snmp.SnmpRowResult)
         */
        public void rowCompleted(SnmpRowResult result) {
            m_rowCount++;
            m_receivedRows.add(result);
            System.err.println("Received Row: "+result);
        }

        public void reset() {
            m_rowCount = 0;
        }

    }

    public SnmpValue value(String val) {
        return SnmpUtils.getValueFactory().getOctetString(val.getBytes());
    }

    public SnmpResult result(SnmpObjId base, String inst) {
        return new SnmpResult(base, new SnmpInstId(inst), value(inst));
    }

    public void verifyRowCount(int expected) {
        assertEquals(expected, m_rowCallback.getRowCount());
    }

    @Before
    public void setUp() throws Exception {
        m_rowCallback = new TestRowCallback();
        m_columns = new SnmpObjId[] { m_ifIndex, m_ifDescr, m_ifMtu };
        m_tableResult = new SnmpTableResult(m_rowCallback, m_columns);
    }
    
    public void anticipateRows(String... instances) {
        for(String inst : instances) {
            SnmpRowResult row = new SnmpRowResult(m_columns.length, new SnmpInstId(inst));
            m_anticipatedRows.add(row);
        }
    }
    
    public void verifyRows() {
        Iterator<SnmpRowResult> anticipated = m_anticipatedRows.iterator();
        Iterator<SnmpRowResult> received = m_receivedRows.iterator();
        
        int count = 1;
        for(; anticipated.hasNext(); ) {
            assertTrue("expected more rows but received none!", received.hasNext());
            SnmpRowResult anticipatedRow = anticipated.next();
            SnmpRowResult receivedRow = received.next();
            assertEquals("Unexpected instance id for row "+count+": ", anticipatedRow.getInstance(), receivedRow.getInstance());
            count++;
        }
        
        if (received.hasNext()) {
            StringBuilder buf = new StringBuilder();
            while(received.hasNext()) {
                buf.append("Unexpected Row: ").append(received.next()).append('\n');
            }
            fail(buf.toString());
        }
        
        m_anticipatedRows.clear();
        m_receivedRows.clear();
    }

    @Test
    public void testSimple() {
        
        anticipateRows("1");

        m_tableResult.storeResult(result(m_ifIndex, "1"));
        m_tableResult.storeResult(result(m_ifDescr, "1"));
        m_tableResult.storeResult(result(m_ifMtu, "1"));

        verifyRows();
        
        anticipateRows("2");

        m_tableResult.storeResult(result(m_ifIndex, "2"));
        m_tableResult.storeResult(result(m_ifDescr, "2"));
        m_tableResult.storeResult(result(m_ifMtu, "2"));

        verifyRows();
        
        for (int i = 0; i < m_columns.length; i++) {
            m_tableResult.columnFinished(m_columns[i]);
        }
        m_tableResult.tableFinished();
        
        verifyRows();
    }

    @Test
    @Ignore
    public void testInitialValueMissingForColumn() {

        anticipateRows("1", "2");
        
        m_tableResult.storeResult(result(m_ifIndex, "1"));
        m_tableResult.storeResult(result(m_ifMtu, "1"));

        /* no way to tell that a result is not coming for ifDescr so no rows
         * expected
         */
        verifyRowCount(0);

        m_tableResult.storeResult(result(m_ifIndex, "2"));
        m_tableResult.storeResult(result(m_ifDescr, "2"));
        m_tableResult.storeResult(result(m_ifMtu, "2"));

        verifyRows();
        
        for (int i = 0; i < m_columns.length; i++) {
            m_tableResult.columnFinished(m_columns[i]);
        }
        m_tableResult.tableFinished();
        
        verifyRows();
    }

}
