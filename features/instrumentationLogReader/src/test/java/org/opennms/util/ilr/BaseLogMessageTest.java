/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.util.ilr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.util.ilr.BaseLogMessage.MsgType;



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
                    "example1/7/192.0.2.12/SNMP",
                    "2010-05-26 12:12:40,785 INFO [CollectdScheduler-50 Pool-fiber4] collector.collect: begin:example1/7/192.0.2.12/SNMP"
                }, 
                { 
                    true,
                    timestamp("2010-05-26 12:12:47,672"), 
                    "CollectdScheduler-50 Pool-fiber12",
                    MsgType.END_COLLECTION,
                    "example1/83/192.0.2.15/SNMP",
                    "2010-05-26 12:12:47,672 INFO [CollectdScheduler-50 Pool-fiber12] collector.collect: end:example1/83/192.0.2.15/SNMP"
                }, 
                { 
                    true,
                    timestamp("2010-05-26 12:12:47,776"),
                    "CollectdScheduler-50 Pool-fiber4",
                    MsgType.ERROR,
                    "example1/7/192.0.2.12/SNMP",
                    "2010-05-26 12:12:47,776 INFO [CollectdScheduler-50 Pool-fiber4] collector.collect: error: example1/7/192.0.2.12/SNMP: org.opennms.netmgt.collectd.CollectionTimedOut: Timeout retrieving SnmpCollectors for 192.0.2.12 for kenny.internal.opennms.com/192.0.2.12: SnmpCollectors for 192.0.2.12: snmpTimeoutError for: kenny.internal.opennms.com/192.0.2.12" 
                }, 
                {
                    true,
                    timestamp("2010-05-26 12:12:48,027"),
                    "CollectdScheduler-50 Pool-fiber11",
                    MsgType.BEGIN_PERSIST,
                    "example1/24/216.216.217.254/SNMP",
                    "2010-05-26 12:12:48,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect: persistDataQueueing: begin: example1/24/216.216.217.254/SNMP"  
                },
                {
                    true,
                    timestamp("2010-05-26 12:12:48,166"),
                    "CollectdScheduler-50 Pool-fiber3",
                    MsgType.END_PERSIST,
                    "example1/63/192.0.2.205/SNMP",
                    "2010-05-26 12:12:48,166 INFO [CollectdScheduler-50 Pool-fiber3] collector.collect: persistDataQueueing: end: example1/63/192.0.2.205/SNMP"
                },
                {
                    false,
                    null,
                    null,
                    null,
                    null,
                    "2010-05-26 12:12:34,414 INFO [CollectdScheduler-50 Pool-fiber0] scheduleFindInterfacesWithService: begin: SNMP"
                },
                {
                    false,
                    null,
                    null,
                    null,
                    null,
                    "Totall bogus log message"
                },
                {
                    false,
                    null,
                    null,
                    null,
                    null,
                    "2013-07-23 11:38:51,997 INFO [LegacyScheduler-Thread-1-of-50] scheduleInterfaceWithService: begin: example1/2647/10.152.128.34/SNMP"
                },
                {
                    true,
                    timestamp("2013-07-23 11:39:22,287"),
                    "LegacyScheduler-Thread-10-of-50",
                    MsgType.BEGIN_COLLECTION,
                    "example1/4489/10.151.27.1/SNMP",
                    "2013-07-23 11:39:22,287 INFO [LegacyScheduler-Thread-10-of-50] collector.collect: begin:example1/4489/10.151.27.1/SNMP"
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

    public BaseLogMessageTest(final boolean msgIsValid, final Date timestamp, final String threadName, final MsgType msgType, final String serviceId, final String logString) {
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
