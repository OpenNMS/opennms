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
package org.opennms.netmgt.rt;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.test.MockLogAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestTrackerTest extends TestCase {
    private static final Logger LOG = LoggerFactory.getLogger(RequestTrackerTest.class);

    /**
     * Test Cases for RtTicketerPlugin
     * 
     * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
     */

    private RequestTracker m_tracker;

    private RTTicket m_ticket;

    /**
     * Don't run this test unless the runRtTests property is set to "true".
     */
    @Override
    protected void runTest() throws Throwable {
        if (!isRunTest()) {
            System.err.println("Skipping test '" + getName() + "' because system property '" + getRunTestProperty() + "' is not set to 'true'");
            return;
        }

        try {
            System.err.println("------------------- begin " + getName() + " ---------------------");
            super.runTest();
        } finally {
            System.err.println("------------------- end " + getName() + " -----------------------");
        }
    }

    private boolean isRunTest() {
        return Boolean.parseBoolean(System.getProperty(getRunTestProperty()));
    }

    private String getRunTestProperty() {
        return "runRtTests";
    }

    @Override
    protected void setUp() throws Exception {
        MockLogAppender.setupLogging();

        final String testHome = System.getProperty("user.home") + File.separatorChar + ".opennms" + File.separatorChar + "test-home";
        final File testProp = new File(testHome + File.separatorChar + "etc" + File.separatorChar + "rt.properties");
        if (testProp.exists()) {
            LOG.debug("{} exists, using it instead of src/test/opennms-home", testHome);
            System.setProperty("opennms.home", testHome);
        } else {
            System.setProperty("opennms.home", "src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");
        }

        RtConfigDao dao = new ReadOnlyRtConfigDao();

        m_tracker = new RequestTracker(dao.getBaseURL(), dao.getUsername(), dao.getPassword(), dao.getTimeout(), dao.getRetry(),
                dao.getUseSystemProxy());
        m_ticket = new RTTicket();
        m_ticket.setQueue(dao.getQueue());
        m_ticket.setStatus("open");
        m_ticket.setSubject("Ticket Subject");
        m_ticket.setText("Ticket Text");
        m_ticket.setRequestor("root@localhost");
    }

    public void testCreateAndGetTicket() throws Exception {
        Long id = m_tracker.createTicket(m_ticket);
        assertTrue(id != 0);
        final RTTicket ticket = m_tracker.getTicket(id, false);
        assertNotNull(ticket);
        assertEquals("Ticket Subject", ticket.getSubject());
    }

    public void testCreateAndUpdateTicket() throws Exception {
        Long id = m_tracker.createTicket(m_ticket);
        assertTrue(id != 0);
        m_tracker.updateTicket(id, "Status: stalled");
        final RTTicket newTicket = m_tracker.getTicket(id, false);
        assertEquals("stalled", newTicket.getStatus());
    }

    public void testCreateMultilineTicket() throws Exception {
        RTTicket ticket = m_ticket.copy();
        final String text = "This is a test.\n\nmultiline";
        ticket.setText(text);
        Long id = m_tracker.createTicket(ticket);
        assertTrue(id != 0);
        ticket = m_tracker.getTicket(id, true);
        assertEquals(text, ticket.getText());
    }
    public void testGetUser() throws Exception {
        RTUser user = m_tracker.getUserInfo("root");
        assertNotNull(user);
        assertNotNull(user.getId());
        assertEquals("Enoch Root", user.getRealname());
    }

    public void testGetQueue() throws Exception {
        RTQueue queue = m_tracker.getQueue(1);
        assertNotNull("queue should not be null", queue);
    }
    
    public void testGetQueuesForUser() throws Exception {
        List<RTQueue> queues = m_tracker.getQueuesForUser("root");
        LOG.debug("queues = {}", queues);
        assertTrue("there must be at least one queue", queues.size() > 0);
    }

    public void testGetTicketsForQueue() throws Exception {
        List<RTTicket> tickets = m_tracker.getTicketsForQueue("General", 10);
        LOG.debug("tickets = {}", tickets);
        assertTrue("there must be at least one ticket", tickets.size() > 0);
    }

}
