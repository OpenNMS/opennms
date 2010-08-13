/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package edu.ncsu.pdgrenon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.*;
import static org.hamcrest.CoreMatchers.*;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.ncsu.pdgrenon.BaseLogMessage;
import edu.ncsu.pdgrenon.BaseLogMessage.MsgType;


/**
 * LogMessageTest
 *
 * @author brozow
 */
@RunWith(Parameterized.class)
public class BaseLogMessageTest {
    
    static Date timestamp(String dateString) throws ParseException {
        return BaseLogMessage.parseTimestamp(dateString);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
                { 
                    true,
                    timestamp("2010-05-26 12:12:40,785"),
                    "CollectdScheduler-50 Pool-fiber4",
                    MsgType.BEGIN_COLLECTION,
                    "7/172.20.1.12/SNMP",
                    "2010-05-26 12:12:40,785 DEBUG [CollectdScheduler-50 Pool-fiber4] Collectd: collector.collect: begin:7/172.20.1.12/SNMP"
                }, 
                { 
                    true,
                    timestamp("2010-05-26 12:12:47,672"), 
                    "CollectdScheduler-50 Pool-fiber12",
                    MsgType.END_COLLECTION,
                    "83/172.20.1.15/SNMP",
                    "2010-05-26 12:12:47,672 DEBUG [CollectdScheduler-50 Pool-fiber12] Collectd: collector.collect: end:83/172.20.1.15/SNMP"
                }, 
                { 
                    true,
                    timestamp("2010-05-26 12:12:47,776"),
                    "CollectdScheduler-50 Pool-fiber4",
                    MsgType.ERROR,
                    "7/172.20.1.12/SNMP",
                    "2010-05-26 12:12:47,776 DEBUG [CollectdScheduler-50 Pool-fiber4] Collectd: collector.collect: error: 7/172.20.1.12/SNMP: org.opennms.netmgt.collectd.CollectionTimedOut: Timeout retrieving SnmpCollectors for 172.20.1.12 for kenny.internal.opennms.com/172.20.1.12: SnmpCollectors for 172.20.1.12: snmpTimeoutError for: kenny.internal.opennms.com/172.20.1.12" 
                }, 
                {
                    true,
                    timestamp("2010-05-26 12:12:48,027"),
                    "CollectdScheduler-50 Pool-fiber11",
                    MsgType.BEGIN_PERSIST,
                    "24/216.216.217.254/SNMP",
                    "2010-05-26 12:12:48,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: persistDataQueueing: begin: 24/216.216.217.254/SNMP"  
                },
                {
                    true,
                    timestamp("2010-05-26 12:12:48,166"),
                    "CollectdScheduler-50 Pool-fiber3",
                    MsgType.END_PERSIST,
                    "63/172.20.1.205/SNMP",
                    "2010-05-26 12:12:48,166 DEBUG [CollectdScheduler-50 Pool-fiber3] Collectd: collector.collect: persistDataQueueing: end: 63/172.20.1.205/SNMP"
                },
                {
                    false,
                    null,
                    null,
                    null,
                    null,
                    "2010-05-26 12:12:34,414 DEBUG [CollectdScheduler-50 Pool-fiber0] Collectd: scheduleFindInterfacesWithService: begin: SNMP"
                },
                {
                    false,
                    null,
                    null,
                    null,
                    null,
                    "Totall bogus log message"
                }
        });
        
    }
    
    private boolean m_msgIsValid;
    private Date m_timestamp;
    private String m_threadName;
    private String m_logString;
    private BaseLogMessage m_logMessage;
    private MsgType m_msgType;
    private String m_serviceId;
    
    public BaseLogMessageTest(boolean msgIsValid, Date timestamp, String threadName, MsgType msgType, String serviceId, String logString) {
        m_msgIsValid = msgIsValid;
        m_timestamp = timestamp;
        m_threadName = threadName;
        m_msgType = msgType;
        m_serviceId = serviceId;
        m_logString = logString;
    }
    
    
    @Before
    public void setUp() {
        m_logMessage = BaseLogMessage.create(m_logString);
    }
    
    
    @Test
    public void testInvalidMessage() {
        assumeThat(m_msgIsValid, is(false));
        assertNull(m_logMessage);
    }

    @Test
    public void testGetTimestamp() {
        assumeThat(m_msgIsValid, is(true));
        assertEquals(m_timestamp, m_logMessage.getDate());
    }
    
    @Test
    public void testGetThreadName() {
        assumeThat(m_msgIsValid, is(true));
        assertEquals(m_threadName, m_logMessage.getThread());
    }
    
    @Test
    public void testGetMsgType() {
        assumeThat(m_msgIsValid, is(true));
        assertEquals(m_msgType, m_logMessage.getMsgType());
        assertTrue(m_logMessage.is(m_msgType));
    }
    
    @Test
    public void testServiceId() {
        assumeThat(m_msgIsValid, is(true));
        assertEquals(m_serviceId, m_logMessage.getServiceID());
    }
    
    
    

}
