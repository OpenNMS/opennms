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
package org.opennms.netmgt.config.dao.outages.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.config.dao.outages.api.OverrideablePollOutagesDao;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml"
})
@JUnitConfigurationEnvironment
public class OverrideablePollOutagesDaoImplTest {
    @Autowired
    private OverrideablePollOutagesDao m_pollOutagesDao;
    File m_configFile = new File("target/test-poller-configuration.xml");

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        String xml = "<?xml version=\"1.0\"?>\n" + 
                "<outages>\n" + 
                "   <outage name=\"one\" type=\"weekly\">\n" + 
                "       <time day=\"sunday\" begins=\"12:30:00\" ends=\"12:45:00\"/>\n" + 
                "       <time day=\"sunday\" begins=\"13:30:00\" ends=\"14:45:00\"/>\n" + 
                "       <time day=\"monday\" begins=\"13:30:00\" ends=\"14:45:00\"/>\n" + 
                "       <time day=\"tuesday\" begins=\"13:00:00\" ends=\"14:45:00\"/>\n" + 
                "       <interface address=\"192.168.0.1\"/>\n" + 
                "       <interface address=\"192.168.0.36\"/>\n" + 
                "       <interface address=\"192.168.0.38\"/>\n" + 
                "   </outage>\n" + 
                "\n" + 
                "   <outage name=\"two\" type=\"monthly\">\n" + 
                "       <time day=\"1\" begins=\"23:30:00\" ends=\"23:45:00\"/>\n" + 
                "       <time day=\"15\" begins=\"21:30:00\" ends=\"21:45:00\"/>\n" + 
                "       <time day=\"15\" begins=\"23:30:00\" ends=\"23:45:00\"/>\n" + 
                "       <interface address=\"192.168.100.254\"/>\n" + 
                "       <interface address=\"192.168.101.254\"/>\n" + 
                "       <interface address=\"192.168.102.254\"/>\n" + 
                "       <interface address=\"192.168.103.254\"/>\n" + 
                "       <interface address=\"192.168.104.254\"/>\n" + 
                "       <interface address=\"192.168.105.254\"/>\n" + 
                "       <interface address=\"192.168.106.254\"/>\n" + 
                "       <interface address=\"192.168.107.254\"/>\n" + 
                "   </outage>\n" + 
                "\n" + 
                "   <outage name=\"three\" type=\"specific\">\n" + 
                "       <time begins=\"21-Feb-2005 05:30:00\" ends=\"21-Feb-2005 15:00:00\"/>\n" + 
                "       <interface address=\"192.168.0.1\"/>\n" + 
                "   </outage>\n";

        final StringBuilder sb = new StringBuilder(xml);

        // Fake a really big poll-outages.xml
        for (int i = 1; i <= 10000; i++) {
            sb.append("<outage name=\"o" + i + "\" type=\"specific\">\n");
            sb.append("<time begins=\"21-Feb-2005 05:30:00\" ends=\"21-Feb-2005 15:00:00\"/>\n");
            sb.append("<node id=\"" + i + "\"/>");
            sb.append("</outage>");
        }
        sb.append("</outages>\n");

        FileWriter w = new FileWriter(m_configFile);
        w.write(sb.toString());
        w.close();
        m_pollOutagesDao.overrideConfig(new FileSystemResource(m_configFile).getInputStream());
        assertEquals(10003, m_pollOutagesDao.getReadOnlyConfig().getOutages().size());
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        FileUtils.deleteQuietly(m_configFile);
    }

    private long getTime(String timeString) throws ParseException {
        Date date = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").parse(timeString);
        return date.getTime();
    }

    @Test
    public void testIsTimeInOutageWeekly() throws Exception {
        long start = System.currentTimeMillis();

        assertTrue(m_pollOutagesDao.isTimeInOutage(getTime("21-FEB-2005 14:00:00"), "one"));
        assertFalse(m_pollOutagesDao.isTimeInOutage(getTime("21-FEB-2005 14:00:00"), "two"));
        assertTrue(m_pollOutagesDao.isTimeInOutage(getTime("21-FEB-2005 14:00:00"), "three"));

        assertTrue(m_pollOutagesDao.isTimeInOutage(getTime("15-FEB-2005 14:00:00"), "one"));
        assertFalse(m_pollOutagesDao.isTimeInOutage(getTime("15-FEB-2005 14:00:00"), "two"));
        assertFalse(m_pollOutagesDao.isTimeInOutage(getTime("15-FEB-2005 14:00:00"), "three"));

        assertFalse(m_pollOutagesDao.isTimeInOutage(getTime("15-FEB-2005 23:37:00"), "one"));
        assertTrue(m_pollOutagesDao.isTimeInOutage(getTime("15-FEB-2005 23:37:00"), "two"));
        assertFalse(m_pollOutagesDao.isTimeInOutage(getTime("15-FEB-2005 23:37:00"), "three"));

        assertFalse(m_pollOutagesDao.isTimeInOutage(getTime("21-FEB-2005 16:00:00"), "one"));
        assertFalse(m_pollOutagesDao.isTimeInOutage(getTime("21-FEB-2005 16:00:00"), "two"));
        assertFalse(m_pollOutagesDao.isTimeInOutage(getTime("21-FEB-2005 16:00:00"), "three"));

        long end = System.currentTimeMillis();
        System.out.println("Test took " + (end-start) + " ms");
    }

    @Test
    public void testPerformance() throws Exception {
        long start = System.currentTimeMillis();
        for (int i = 1; i <= 200; i++) {
            String outageName = "o" + Integer.toString(i);
            Outage outage = m_pollOutagesDao.getReadOnlyConfig().getOutage(outageName);
            assertTrue(outage != null);
            assertTrue(m_pollOutagesDao.isNodeIdInOutage(i, outageName));
        }
        long end = System.currentTimeMillis();
        System.out.println("Test took " + (end-start) + " ms");
    }
}
