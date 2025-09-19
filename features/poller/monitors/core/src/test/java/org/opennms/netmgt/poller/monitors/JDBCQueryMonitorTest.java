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
package org.opennms.netmgt.poller.monitors;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TestName;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.ClassRule;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.SimpleMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author schlazor
 */
public class JDBCQueryMonitorTest {
    private static final Logger LOG = LoggerFactory.getLogger(JDBCQueryMonitorTest.class);

    @Rule
    public TestName m_test = new TestName();

    @Before
    public void startUp() throws Exception {
        LOG.info("======== Starting test " + m_test.getMethodName());
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("======== Finished test " + m_test.getMethodName());
    }

    /**
     * Test of poll method with minimal config.
     */
    @Test
    public void testPoll() {
        try {
            try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
                ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
                when(rsmd.getColumnCount()).thenReturn(3);
                ResultSet rs = mock(ResultSet.class);
                when(rs.first()).thenReturn(true);
                when(rs.last()).thenReturn(true);
                when(rs.getRow()).thenReturn(1);
                when(rs.getMetaData()).thenReturn(rsmd);
                Statement stmt = mock(Statement.class);
                when(stmt.executeQuery("select * from events")).thenReturn(rs);
                Connection con = mock(Connection.class);
                when(con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);
                Properties p = new Properties();
                p.setProperty("user", "postgres");
                p.setProperty("password", "");
                p.setProperty("timeout", "3");
                Driver driver = mock(Driver.class);
                when(driver.connect("jdbc:postgresql://localhost/opennms",p)).thenReturn(con);
                mockedDriverManager.when(() -> DriverManager.getDriver(eq("jdbc:postgresql://localhost/opennms"))).thenReturn(driver);
                MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "JDBCQueryMonitor");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("query", "select * from events");
                parameters.put("operand", "1");
                JDBCQueryMonitor instance = new JDBCQueryMonitor();
                PollStatus result = instance.poll(svc, parameters);
                LOG.info("poll result: {}, reason: {}", result.getStatusName(), result.getReason());
                assertEquals(PollStatus.SERVICE_AVAILABLE, result.getStatusCode());
            }
        } catch (Exception e) {
            LOG.info("Exception: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test of poll method with query included in failure reason
     */
    @Test
    public void testPollFailureWithQuery() {
        try {
            try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
                ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
                when(rsmd.getColumnCount()).thenReturn(3);
                ResultSet rs = mock(ResultSet.class);
                when(rs.first()).thenReturn(true);
                when(rs.last()).thenReturn(true);
                when(rs.getRow()).thenReturn(1);
                when(rs.getMetaData()).thenReturn(rsmd);
                Statement stmt = mock(Statement.class);
                when(stmt.executeQuery("select * from events")).thenReturn(rs);
                Connection con = mock(Connection.class);
                when(con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);
                Properties p = new Properties();
                p.setProperty("user", "postgres");
                p.setProperty("password", "");
                p.setProperty("timeout", "3");
                Driver driver = mock(Driver.class);
                when(driver.connect("jdbc:postgresql://localhost/opennms",p)).thenReturn(con);
                mockedDriverManager.when(() -> DriverManager.getDriver(eq("jdbc:postgresql://localhost/opennms"))).thenReturn(driver);
                MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "JDBCQueryMonitor");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("query", "select * from events");
                parameters.put("operand", "2");
                parameters.put("include-query", "true");
                JDBCQueryMonitor instance = new JDBCQueryMonitor();
                PollStatus result = instance.poll(svc, parameters);
                LOG.info("poll result: {}, reason: {}", result.getStatusName(), result.getReason());
                assertEquals(PollStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
                assertEquals("Row Count Check Failed: 1 >= 2\nQuery: select * from events", result.getReason());
            }
        } catch (Exception e) {
            LOG.info("Exception: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test of poll method with first row included in failure reason
     */
    @Test
    public void testPollFailureWithFirstResult() {
        try {
            try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
                ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
                when(rsmd.getColumnCount()).thenReturn(3);
                ResultSet rs = mock(ResultSet.class);
                when(rs.first()).thenReturn(true);
                when(rs.last()).thenReturn(true);
                when(rs.getRow()).thenReturn(1,1);
                when(rs.getMetaData()).thenReturn(rsmd);
                when(rs.next()).thenReturn(false);
                when(rsmd.getColumnName(1)).thenReturn("eventid");
                when(rsmd.getColumnName(2)).thenReturn("eventuei");
                when(rsmd.getColumnName(3)).thenReturn("nodeid");
                when(rs.getString(1)).thenReturn("1337");
                when(rs.getString(2)).thenReturn("uei.opennms.org/nodes/nodeLostService");
                when(rs.getString(3)).thenReturn("1");
                Statement stmt = mock(Statement.class);
                when(stmt.executeQuery("select * from events")).thenReturn(rs);
                Connection con = mock(Connection.class);
                when(con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);
                Properties p = new Properties();
                p.setProperty("user", "postgres");
                p.setProperty("password", "");
                p.setProperty("timeout", "3");
                Driver driver = mock(Driver.class);
                when(driver.connect("jdbc:postgresql://localhost/opennms",p)).thenReturn(con);
                mockedDriverManager.when(() -> DriverManager.getDriver(eq("jdbc:postgresql://localhost/opennms"))).thenReturn(driver);
                MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "JDBCQueryMonitor");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("query", "select * from events");
                parameters.put("operand", "2");
                parameters.put("include-first-result", "true");
                JDBCQueryMonitor instance = new JDBCQueryMonitor();
                PollStatus result = instance.poll(svc, parameters);
                LOG.info("poll result: {}, reason: {}", result.getStatusName(), result.getReason());
                assertEquals(PollStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
                assertEquals("Row Count Check Failed: 1 >= 2\nResults:\neventid: 1337; eventuei: uei.opennms.org/nodes/nodeLostService; nodeid: 1", result.getReason());
            }
        } catch (Exception e) {
            LOG.info("Exception: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test of poll method with first row of many included in failure reason
     */
    @Test
    public void testPollFailureWithFirstResultOfMany() {
        try {
            try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
                ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
                when(rsmd.getColumnCount()).thenReturn(3);
                ResultSet rs = mock(ResultSet.class);
                when(rs.first()).thenReturn(true);
                when(rs.last()).thenReturn(true);
                when(rs.getRow()).thenReturn(1,2);
                when(rs.getMetaData()).thenReturn(rsmd);
                when(rs.next()).thenReturn(true, false);
                when(rsmd.getColumnName(1)).thenReturn("eventid");
                when(rsmd.getColumnName(2)).thenReturn("eventuei");
                when(rsmd.getColumnName(3)).thenReturn("nodeid");
                when(rs.getString(1)).thenReturn("1337", "1338");
                when(rs.getString(2)).thenReturn("uei.opennms.org/nodes/nodeLostService", "uei.opennms.org/nodes/nodeDown");
                when(rs.getString(3)).thenReturn("1", "2");
                Statement stmt = mock(Statement.class);
                when(stmt.executeQuery("select * from events")).thenReturn(rs);
                Connection con = mock(Connection.class);
                when(con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);
                Properties p = new Properties();
                p.setProperty("user", "postgres");
                p.setProperty("password", "");
                p.setProperty("timeout", "3");
                Driver driver = mock(Driver.class);
                when(driver.connect("jdbc:postgresql://localhost/opennms",p)).thenReturn(con);
                mockedDriverManager.when(() -> DriverManager.getDriver(eq("jdbc:postgresql://localhost/opennms"))).thenReturn(driver);
                MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "JDBCQueryMonitor");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("query", "select * from events");
                parameters.put("operand", "3");
                parameters.put("include-first-result", "true");
                JDBCQueryMonitor instance = new JDBCQueryMonitor();
                PollStatus result = instance.poll(svc, parameters);
                LOG.info("poll result: {}, reason: {}", result.getStatusName(), result.getReason());
                assertEquals(PollStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
                assertEquals("Row Count Check Failed: 2 >= 3\nResults:\neventid: 1337; eventuei: uei.opennms.org/nodes/nodeLostService; nodeid: 1", result.getReason());
            }
        } catch (Exception e) {
            LOG.info("Exception: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test of poll method with all rows of many included in failure reason
     */
    @Test
    public void testPollFailureWithAllResultsOfMany() {
        try {
            try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
                ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
                when(rsmd.getColumnCount()).thenReturn(3);
                ResultSet rs = mock(ResultSet.class);
                when(rs.first()).thenReturn(true);
                when(rs.last()).thenReturn(true);
                when(rs.getRow()).thenReturn(1,2);
                when(rs.getMetaData()).thenReturn(rsmd);
                when(rs.next()).thenReturn(true, false);
                when(rsmd.getColumnName(1)).thenReturn("eventid");
                when(rsmd.getColumnName(2)).thenReturn("eventuei");
                when(rsmd.getColumnName(3)).thenReturn("nodeid");
                when(rs.getString(1)).thenReturn("1337", "1338");
                when(rs.getString(2)).thenReturn("uei.opennms.org/nodes/nodeLostService", "uei.opennms.org/nodes/nodeDown");
                when(rs.getString(3)).thenReturn("1", "2");
                Statement stmt = mock(Statement.class);
                when(stmt.executeQuery("select * from events")).thenReturn(rs);
                Connection con = mock(Connection.class);
                when(con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);
                Properties p = new Properties();
                p.setProperty("user", "postgres");
                p.setProperty("password", "");
                p.setProperty("timeout", "3");
                Driver driver = mock(Driver.class);
                when(driver.connect("jdbc:postgresql://localhost/opennms",p)).thenReturn(con);
                mockedDriverManager.when(() -> DriverManager.getDriver(eq("jdbc:postgresql://localhost/opennms"))).thenReturn(driver);
                MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "JDBCQueryMonitor");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("query", "select * from events");
                parameters.put("operand", "3");
                parameters.put("include-first-result", "true"); // this gets ignored as the next parameter takes precedence
                parameters.put("include-all-results", "true");
                JDBCQueryMonitor instance = new JDBCQueryMonitor();
                PollStatus result = instance.poll(svc, parameters);
                LOG.info("poll result: {}, reason: {}", result.getStatusName(), result.getReason());
                assertEquals(PollStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
                assertEquals("Row Count Check Failed: 2 >= 3\nResults:\neventid: 1337; eventuei: uei.opennms.org/nodes/nodeLostService; nodeid: 1\neventid: 1338; eventuei: uei.opennms.org/nodes/nodeDown; nodeid: 2", result.getReason());
            }
        } catch (Exception e) {
            LOG.info("Exception: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test of poll method with all rows of many included in failure reason where include-first-result is false
     */
    @Test
    public void testPollFailureWithAllResultsOfMore() {
        try {
            try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
                ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
                when(rsmd.getColumnCount()).thenReturn(3);
                ResultSet rs = mock(ResultSet.class);
                when(rs.first()).thenReturn(true);
                when(rs.last()).thenReturn(true);
                when(rs.getRow()).thenReturn(1, 2, 3);
                when(rs.getMetaData()).thenReturn(rsmd);
                when(rs.next()).thenReturn(true, true, false);
                when(rsmd.getColumnName(1)).thenReturn("eventid");
                when(rsmd.getColumnName(2)).thenReturn("eventuei");
                when(rsmd.getColumnName(3)).thenReturn("nodeid");
                when(rs.getString(1)).thenReturn("1337", "1338", "1339");
                when(rs.getString(2)).thenReturn("uei.opennms.org/nodes/nodeLostService", "uei.opennms.org/nodes/nodeDown", "uei.opennms.org/nodes/nodeUp");
                when(rs.getString(3)).thenReturn("1", "2");
                Statement stmt = mock(Statement.class);
                when(stmt.executeQuery("select * from events")).thenReturn(rs);
                Connection con = mock(Connection.class);
                when(con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);
                Properties p = new Properties();
                p.setProperty("user", "postgres");
                p.setProperty("password", "");
                p.setProperty("timeout", "3");
                Driver driver = mock(Driver.class);
                when(driver.connect("jdbc:postgresql://localhost/opennms",p)).thenReturn(con);
                mockedDriverManager.when(() -> DriverManager.getDriver(eq("jdbc:postgresql://localhost/opennms"))).thenReturn(driver);
                MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "JDBCQueryMonitor");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("query", "select * from events");
                parameters.put("operand", "4");
                parameters.put("include-first-result", "false"); // this gets ignored as the next parameter takes precedence
                parameters.put("include-all-results", "true");
                JDBCQueryMonitor instance = new JDBCQueryMonitor();
                PollStatus result = instance.poll(svc, parameters);
                LOG.info("poll result: {}, reason: {}", result.getStatusName(), result.getReason());
                assertEquals(PollStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
                assertEquals("Row Count Check Failed: 3 >= 4\nResults:\neventid: 1337; eventuei: uei.opennms.org/nodes/nodeLostService; nodeid: 1\neventid: 1338; eventuei: uei.opennms.org/nodes/nodeDown; nodeid: 2\neventid: 1339; eventuei: uei.opennms.org/nodes/nodeUp; nodeid: 2", result.getReason());
            }
        } catch (Exception e) {
            LOG.info("Exception: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test of poll method with query and all rows of many included in failure reason where include-first-result is false
     */
    @Test
    public void testPollFailureWithQueryAndAllResultsOfMore() {
        try {
            try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
                ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
                when(rsmd.getColumnCount()).thenReturn(3);
                ResultSet rs = mock(ResultSet.class);
                when(rs.first()).thenReturn(true);
                when(rs.last()).thenReturn(true);
                when(rs.getRow()).thenReturn(1, 2, 3);
                when(rs.getMetaData()).thenReturn(rsmd);
                when(rs.next()).thenReturn(true, true, false);
                when(rsmd.getColumnName(1)).thenReturn("eventid");
                when(rsmd.getColumnName(2)).thenReturn("eventuei");
                when(rsmd.getColumnName(3)).thenReturn("nodeid");
                when(rs.getString(1)).thenReturn("1337", "1338", "1339");
                when(rs.getString(2)).thenReturn("uei.opennms.org/nodes/nodeLostService", "uei.opennms.org/nodes/nodeDown", "uei.opennms.org/nodes/nodeUp");
                when(rs.getString(3)).thenReturn("1", "2");
                Statement stmt = mock(Statement.class);
                when(stmt.executeQuery("select * from events")).thenReturn(rs);
                Connection con = mock(Connection.class);
                when(con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);
                Properties p = new Properties();
                p.setProperty("user", "postgres");
                p.setProperty("password", "");
                p.setProperty("timeout", "3");
                Driver driver = mock(Driver.class);
                when(driver.connect("jdbc:postgresql://localhost/opennms",p)).thenReturn(con);
                mockedDriverManager.when(() -> DriverManager.getDriver(eq("jdbc:postgresql://localhost/opennms"))).thenReturn(driver);
                MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "JDBCQueryMonitor");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("query", "select * from events");
                parameters.put("operand", "4");
                parameters.put("include-first-result", "false"); // this gets ignored as the next parameter takes precedence
                parameters.put("include-all-results", "true");
                parameters.put("include-query", "true");
                JDBCQueryMonitor instance = new JDBCQueryMonitor();
                PollStatus result = instance.poll(svc, parameters);
                LOG.info("poll result: {}, reason: {}", result.getStatusName(), result.getReason());
                assertEquals(PollStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
                assertEquals("Row Count Check Failed: 3 >= 4\nQuery: select * from events\nResults:\neventid: 1337; eventuei: uei.opennms.org/nodes/nodeLostService; nodeid: 1\neventid: 1338; eventuei: uei.opennms.org/nodes/nodeDown; nodeid: 2\neventid: 1339; eventuei: uei.opennms.org/nodes/nodeUp; nodeid: 2", result.getReason());
            }
        } catch (Exception e) {
            LOG.info("Exception: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test of poll method with query and all rows of many included in failure reason where include-first-result is false with alternative line, column, and column key separators
     */
    @Test
    public void testPollFailureWithQueryAndAllResultsOfMoreWithSeparators() {
        try {
            try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
                ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
                when(rsmd.getColumnCount()).thenReturn(3);
                ResultSet rs = mock(ResultSet.class);
                when(rs.first()).thenReturn(true);
                when(rs.last()).thenReturn(true);
                when(rs.getRow()).thenReturn(1, 2, 3);
                when(rs.getMetaData()).thenReturn(rsmd);
                when(rs.next()).thenReturn(true, true, false);
                when(rsmd.getColumnName(1)).thenReturn("eventid");
                when(rsmd.getColumnName(2)).thenReturn("eventuei");
                when(rsmd.getColumnName(3)).thenReturn("nodeid");
                when(rs.getString(1)).thenReturn("1337", "1338", "1339");
                when(rs.getString(2)).thenReturn("uei.opennms.org/nodes/nodeLostService", "uei.opennms.org/nodes/nodeDown", "uei.opennms.org/nodes/nodeUp");
                when(rs.getString(3)).thenReturn("1", "2");
                Statement stmt = mock(Statement.class);
                when(stmt.executeQuery("select * from events")).thenReturn(rs);
                Connection con = mock(Connection.class);
                when(con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);
                Properties p = new Properties();
                p.setProperty("user", "postgres");
                p.setProperty("password", "");
                p.setProperty("timeout", "3");
                Driver driver = mock(Driver.class);
                when(driver.connect("jdbc:postgresql://localhost/opennms",p)).thenReturn(con);
                mockedDriverManager.when(() -> DriverManager.getDriver(eq("jdbc:postgresql://localhost/opennms"))).thenReturn(driver);
                MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "JDBCQueryMonitor");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("query", "select * from events");
                parameters.put("operand", "4");
                parameters.put("include-first-result", "false"); // this gets ignored as the next parameter takes precedence
                parameters.put("include-all-results", "true");
                parameters.put("include-query", "true");
                parameters.put("line-separator", "<br/>");
                parameters.put("column-separator", "&nbsp;");
                parameters.put("column-key-separator", " - ");
                JDBCQueryMonitor instance = new JDBCQueryMonitor();
                PollStatus result = instance.poll(svc, parameters);
                LOG.info("poll result: {}, reason: {}", result.getStatusName(), result.getReason());
                assertEquals(PollStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
                assertEquals("Row Count Check Failed: 3 >= 4<br/>Query: select * from events<br/>Results:<br/>eventid - 1337&nbsp;eventuei - uei.opennms.org/nodes/nodeLostService&nbsp;nodeid - 1<br/>eventid - 1338&nbsp;eventuei - uei.opennms.org/nodes/nodeDown&nbsp;nodeid - 2<br/>eventid - 1339&nbsp;eventuei - uei.opennms.org/nodes/nodeUp&nbsp;nodeid - 2", result.getReason());
            }
        } catch (Exception e) {
            LOG.info("Exception: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
    /**
     * Test of poll method with query and first row of many included in failure reason
     */
    @Test
    public void testPollFailureWithQueryAndFirstResultOfMore() {
        try {
            try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
                ResultSetMetaData rsmd = mock(ResultSetMetaData.class);
                when(rsmd.getColumnCount()).thenReturn(3);
                ResultSet rs = mock(ResultSet.class);
                when(rs.first()).thenReturn(true);
                when(rs.last()).thenReturn(true);
                when(rs.getRow()).thenReturn(1, 2, 3);
                when(rs.getMetaData()).thenReturn(rsmd);
                when(rs.next()).thenReturn(true, true, false);
                when(rsmd.getColumnName(1)).thenReturn("eventid");
                when(rsmd.getColumnName(2)).thenReturn("eventuei");
                when(rsmd.getColumnName(3)).thenReturn("nodeid");
                when(rs.getString(1)).thenReturn("1337", "1338", "1339");
                when(rs.getString(2)).thenReturn("uei.opennms.org/nodes/nodeLostService", "uei.opennms.org/nodes/nodeDown", "uei.opennms.org/nodes/nodeUp");
                when(rs.getString(3)).thenReturn("1", "2");
                Statement stmt = mock(Statement.class);
                when(stmt.executeQuery("select * from events")).thenReturn(rs);
                Connection con = mock(Connection.class);
                when(con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);
                Properties p = new Properties();
                p.setProperty("user", "postgres");
                p.setProperty("password", "");
                p.setProperty("timeout", "3");
                Driver driver = mock(Driver.class);
                when(driver.connect("jdbc:postgresql://localhost/opennms",p)).thenReturn(con);
                mockedDriverManager.when(() -> DriverManager.getDriver(eq("jdbc:postgresql://localhost/opennms"))).thenReturn(driver);
                MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "JDBCQueryMonitor");
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("query", "select * from events");
                parameters.put("operand", "4");
                parameters.put("include-first-result", "true");
                parameters.put("include-query", "true");
                JDBCQueryMonitor instance = new JDBCQueryMonitor();
                PollStatus result = instance.poll(svc, parameters);
                LOG.info("poll result: {}, reason: {}", result.getStatusName(), result.getReason());
                assertEquals(PollStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
                assertEquals("Row Count Check Failed: 3 >= 4\nQuery: select * from events\nResults:\neventid: 1337; eventuei: uei.opennms.org/nodes/nodeLostService; nodeid: 1", result.getReason());
            }
        } catch (Exception e) {
            LOG.info("Exception: {}", e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
