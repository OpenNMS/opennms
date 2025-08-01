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
package org.opennms.util.ilr;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Date;

import org.junit.Test;
import org.opennms.util.ilr.LogMessage;
import org.opennms.util.ilr.SimpleLogMessage;


public class SimpleLogMessageTest {
    @Test
    public void testGetService() {
        LogMessage log = SimpleLogMessage.create("2010-05-26 12:12:38,027 DEBUG [CollectdScheduler-50 Pool-fiber11] Collectd: collector.collect: collectData: end: 24/216.216.217.254/SNMP");
        assertEquals("24/216.216.217.254/SNMP", log.getServiceID());
    }
    @Test
    public void testPostLog4jRefactorService() throws Exception {
        LogMessage log = SimpleLogMessage.create("2013-07-23 11:39:22,295 DEBUG [LegacyScheduler-Thread-34-of-50] o.o.n.c.DefaultCollectdInstrumentation: collector.collect: begin:816/10.151.24.66/SNMP");
        assertEquals("816/10.151.24.66/SNMP", log.getServiceID());
        assertEquals("LegacyScheduler-Thread-34-of-50", log.getThread());
        assertEquals(timestamp("2013-07-23 11:39:22,295"), log.getDate());
    }
    
    static Date timestamp(final String dateString) throws ParseException {
        return BaseLogMessage.parseTimestamp(dateString);
    }
}
