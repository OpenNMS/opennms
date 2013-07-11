/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
        return Boolean.getBoolean(getRunTestProperty());
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

    private void assertTicketEquals(final Ticket existing, final Ticket retrieved) {
        assertEquals(existing.getId(), retrieved.getId());
        assertEquals(existing.getState(), retrieved.getState());
        assertEquals(existing.getUser(), retrieved.getUser());
        assertEquals(existing.getSummary(), retrieved.getSummary());
    }

}
