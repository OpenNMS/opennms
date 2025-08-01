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
package org.opennms.netmgt.tl1d;

import java.text.SimpleDateFormat;

import junit.framework.TestCase;

public class Tl1AutonomousMessageProcessorTest extends TestCase {
    
    Tl1AutonomousMessageProcessor m_processor = new Tl1AutonomousMessageProcessor();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
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
        Class.forName("org.opennms.netmgt.tl1d.Tl1ClientImpl").newInstance();
    }

}
