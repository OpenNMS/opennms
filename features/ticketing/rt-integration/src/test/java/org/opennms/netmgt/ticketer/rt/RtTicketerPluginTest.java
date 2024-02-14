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
package org.opennms.netmgt.ticketer.rt;

import java.io.File;
import java.util.Date;

import junit.framework.TestCase;

import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RtTicketerPluginTest extends TestCase {
    private static final Logger LOG = LoggerFactory.getLogger(RtTicketerPluginTest.class);

    /**
     * Test Cases for RtTicketerPlugin
     * 
     * @author <a href="mailto:jonathan@opennms.org">Jonathan Sartin</a>
     */

    RtTicketerPlugin m_ticketer;

    Ticket m_ticket;

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
        final String testHome = System.getProperty("user.home") + File.separatorChar + ".opennms" + File.separatorChar + "test-home";
        final File testProp = new File(testHome + File.separatorChar + "etc" + File.separatorChar + "rt.properties");
        if (testProp.exists()) {
            LOG.debug("{} exists, using it instead of src/test/opennms-home", testHome);
            System.setProperty("opennms.home", testHome);
        } else {
            System.setProperty("opennms.home", "src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");
        }

        m_ticketer = new RtTicketerPlugin();

        m_ticket = new Ticket();
        m_ticket.setState(Ticket.State.OPEN);
        m_ticket.setSummary("Ticket Summary for ticket: " + new Date());
        m_ticket.setDetails("First Article for ticket: " + new Date());
        m_ticket.setUser("root@localhost");

    }

    public void testSaveAndGet() {

        try {
            m_ticketer.saveOrUpdate(m_ticket);
            Ticket retrievedTicket = m_ticketer.get(m_ticket.getId());
            assertTicketEquals(m_ticket, retrievedTicket);
        } catch (final PluginException e) {
            e.printStackTrace();
            fail("Something failed in the ticketer plugin");
        }

    }

    public void testUpdateAndGet() {

        try {
            m_ticketer.saveOrUpdate(m_ticket);
            Ticket savedTicket = m_ticketer.get(m_ticket.getId());
            assertTicketEquals(m_ticket, savedTicket);
            m_ticket.setState(Ticket.State.CLOSED);
            m_ticketer.saveOrUpdate(m_ticket);
            System.out.println("before update, ticket status was " + savedTicket.getState().toString());
            Ticket updatedTicket = m_ticketer.get(m_ticket.getId());
            System.out.println("after update, ticket status was " + updatedTicket.getState().toString());
            assertTicketEquals(m_ticket, updatedTicket);
        } catch (PluginException e) {
            fail("Something failed in the ticketer plugin");
            e.printStackTrace();
        }

    }

    public void testStateConversions() {
        assertEquals(Ticket.State.OPEN, m_ticketer.rtToOpenNMSState("open"));
        assertEquals(Ticket.State.OPEN, m_ticketer.rtToOpenNMSState("new"));
        assertEquals(Ticket.State.CLOSED, m_ticketer.rtToOpenNMSState("resolved"));
        assertEquals(Ticket.State.CANCELLED, m_ticketer.rtToOpenNMSState("cancelled"));
        assertEquals(Ticket.State.OPEN, m_ticketer.rtToOpenNMSState("INVALID_STRING"));

        assertEquals("new", m_ticketer.openNMSToRTState(Ticket.State.OPEN));
        assertEquals("resolved", m_ticketer.openNMSToRTState(Ticket.State.CLOSED));
        assertEquals("cancelled", m_ticketer.openNMSToRTState(Ticket.State.CANCELLED));
    }

    private void assertTicketEquals(final Ticket existing, final Ticket retrieved) {
        assertEquals(existing.getId(), retrieved.getId());
        assertEquals(existing.getState(), retrieved.getState());
        assertEquals(existing.getUser(), retrieved.getUser());
        assertEquals(existing.getSummary(), retrieved.getSummary());
    }

}
