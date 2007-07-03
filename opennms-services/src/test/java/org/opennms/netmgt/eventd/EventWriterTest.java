//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.eventd;


import java.sql.SQLException;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.dao.db.PopulatedTemporaryDatabaseTestCase;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.trapd.EventConstants;
import org.opennms.netmgt.utils.EventBuilder;

/**
 * This class tests some of the quirky behaviors of presisting events.
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class EventWriterTest extends PopulatedTemporaryDatabaseTestCase {
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        DataSourceFactory.setInstance(getDataSource());
    }

    public void testInitializeDataSourceFactory() {
        assertSame(getDataSource(), DataSourceFactory.getDataSource());
    }

    /**
     * tests sequence of newly initialized db
     */
    public void testNextEventId() {
        int nextId = getJdbcTemplate().queryForInt("SELECT nextval('eventsNxtId')");
        System.err.println(nextId);
        
        // an empty db should produce '1' here
        assertEquals(1, nextId);
    }

    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    public void testWriteEventWithNull() throws SQLException {
        
        EventWriter eWriter = new EventWriter("SELECT nextval('eventsNxtId')");
        
        try {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest("logndisplay");
        bldr.addParam("test", "testVal");
        bldr.addParam("test2", "valWith\u0000Null\u0000");
        
        byte[] bytes = new byte[] { 0x07, (byte)0xD7, 0x04, 0x0A, 0x01, 0x17, 0x06, 0x00, 0x2B, 0x00, 0x00 };
        

        SnmpValue snmpVal = SnmpUtils.getValueFactory().getOctetString(bytes);
        
        assertFalse(snmpVal.isDisplayable());
        
        bldr.addParam("test3", snmpVal.toString());
        
        String b64 = EventConstants.toString(EventConstants.XML_ENCODING_BASE64, snmpVal);
        
        System.err.println(b64);
        
        bldr.addParam("test", b64);
        
        System.err.println(snmpVal.toString());
        
        eWriter.persistEvent(null, bldr.getEvent());
        
        } finally {
        eWriter.close();
        }
    }

    /**
     * Tests writing nulls to postgres db and the db encoding.
     * @throws SQLException
     */
    public void testWriteEventDescrWithNull() throws SQLException {
        
        EventWriter eWriter = new EventWriter("SELECT nextval('eventsNxtId')");
        
        try {
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest("logndisplay");
        
        bldr.setDescription("abc\u0000def");
        
        eWriter.persistEvent(null, bldr.getEvent());
        
        } finally {
        eWriter.close();
        }
    }

}
