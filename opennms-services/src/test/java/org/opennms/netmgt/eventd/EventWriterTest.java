package org.opennms.netmgt.eventd;


import java.sql.SQLException;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.dao.db.PopulatedTemporaryDatabaseTestCase;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.trapd.EventConstants;
import org.opennms.netmgt.utils.EventBuilder;

public class EventWriterTest extends PopulatedTemporaryDatabaseTestCase {
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        DataSourceFactory.setInstance(getDataSource());
    }

    public void testInitializeDataSourceFactory() {
        assertSame(getDataSource(), DataSourceFactory.getDataSource());
    }
    
    public void testNextEventId() {
        int nextId = getJdbcTemplate().queryForInt("SELECT nextval('eventsNxtId')");
        System.err.println(nextId);
        
        // an empty db should produce '1' here
        assertEquals(1, nextId);
    }
    
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
