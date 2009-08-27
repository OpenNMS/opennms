/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created August 3, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.tl1d;

import java.text.SimpleDateFormat;

import junit.framework.TestCase;

public class Tl1AutonomousMessageProcessorTest extends TestCase {
    
    Tl1AutonomousMessageProcessor m_processor = new Tl1AutonomousMessageProcessor();

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testProcess() {
        
        String sampleMessage = "   GPON15000 2008-07-31 18:29:49\n" + 
        		"*C 0 REPT ALM BITS\n" + 
        		"   \"1-4:NTFCNCDE=CR,CONDTYPE=FAIL,SRVEFF=SA,OCRDAT=09-23,OCRTM=02-03-04,LOCN=NEND,DIRN=RCV\"\n" + 
        		";\n" + 
        		"";
        
        Tl1AutonomousMessage alarm = m_processor.process(sampleMessage, Tl1Message.AUTONOMOUS);
        
        assertNotNull(alarm.getRawMessage());
        assertNotNull(alarm.getHeader());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2008-07-31", formatter.format(alarm.getHeader().getTimestamp()));
        formatter = new SimpleDateFormat("HH:mm:ss");
        assertEquals("18:29:49", formatter.format(alarm.getHeader().getTimestamp()));
        assertEquals("GPON15000", alarm.getHost());
        assertEquals("GPON15000", alarm.getHeader().getSid());
        assertEquals(alarm.getTimestamp(), alarm.getHeader().getTimestamp());
        
        assertEquals("*C 0 REPT ALM BITS", alarm.getId().getRawMessage());
        assertEquals("*C", alarm.getId().getAlarmCode());
        assertEquals("0", alarm.getId().getAlarmTag());
        assertEquals("REPT ALM BITS", alarm.getId().getVerb());
        assertEquals("\"1-4:NTFCNCDE=CR,CONDTYPE=FAIL,SRVEFF=SA,OCRDAT=09-23,OCRTM=02-03-04,LOCN=NEND,DIRN=RCV\"", alarm.getAutoBlock().getBlock());
        assertEquals("CR", alarm.getAutoBlock().getNtfcncde());
        
    }
    
    /**
     * Presumed Alcatel example from opennms-discuss mailing list, 24-Aug-2009
     * Two-digit year in header, bare NTFCNCDE value in auto block (both appear to be legal)
     * http://marc.info/?l=opennms-discuss&m=125112385300943&w=2
     */
    public void testProcessAlcatel() {
        
        String sampleMessage = "DSALC003 09-04-20 07:38:35\n" + 
                "** 169 REPT ALM ENV\n" + 
                "   \"ENV-2:MJ,MISC,4-7,7-30-15,\\\"Miscellaneous environment alarm\\\"\"\n" + 
                ";\n" + 
                "";
        
        Tl1AutonomousMessage alarm = m_processor.process(sampleMessage, Tl1Message.AUTONOMOUS);
        
        assertNotNull(alarm.getRawMessage());
        assertNotNull(alarm.getHeader());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2009-04-20", formatter.format(alarm.getHeader().getTimestamp()));
        formatter = new SimpleDateFormat("HH:mm:ss");
        assertEquals("07:38:35", formatter.format(alarm.getHeader().getTimestamp()));
        assertEquals("DSALC003", alarm.getHost());
        assertEquals("DSALC003", alarm.getHeader().getSid());
        assertEquals(alarm.getTimestamp(), alarm.getHeader().getTimestamp());
        
        assertEquals("** 169 REPT ALM ENV", alarm.getId().getRawMessage());
        assertEquals("**", alarm.getId().getAlarmCode());
        assertEquals("169", alarm.getId().getAlarmTag());
        assertEquals("REPT ALM ENV", alarm.getId().getVerb());
        assertEquals("\"ENV-2:MJ,MISC,4-7,7-30-15,\\\"Miscellaneous environment alarm\\\"\"", alarm.getAutoBlock().getBlock());
        assertEquals("MJ", alarm.getAutoBlock().getNtfcncde());
        
    }
    
    public void testInstanitateClass() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Tl1Client client = (Tl1Client) Class.forName("org.opennms.netmgt.tl1d.Tl1ClientImpl").newInstance();
    }

}
