/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.jira;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;

@Ignore("These tests rely on the Jira system to be configured correctly (see jira.properties in src/test/resources/opennms-home/etc/jira.properties)")
public class JiraTicketerPluginTest {

    JiraTicketerPlugin m_ticketer;

    @Before
    public void setUp() throws Exception {
        final File opennmsHome = Paths.get("src", "test", "resources", "opennms-home").toFile();
        assertTrue(opennmsHome + " must exist.", opennmsHome.exists());
        System.setProperty("opennms.home", opennmsHome.getAbsolutePath());

        m_ticketer = new JiraTicketerPlugin();
    }

    @Test
    @Ignore
    public void verifyTooManyFiles() throws PluginException {
        JiraTicketerPlugin plugin = new JiraTicketerPlugin();
        for (int i=0; i<500; i++) {
            System.out.print(i + ": ");
            Ticket ticket = plugin.get("NMS-8947");
            System.out.print(ticket.getSummary() + "\n");
        }
    }

    @Test
    @Ignore
    public void canSaveGetAndUpdate() throws Exception {
        String ticketId = save();
        get(ticketId);
        update(ticketId);
    }

    private String save() throws PluginException {
        Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary("This is the summary");
        ticket.setDetails("These are the details");

        m_ticketer.saveOrUpdate(ticket);

        assertNotNull(ticket.getId());

        Ticket newTicket = m_ticketer.get(ticket.getId());

        assertNotNull(newTicket);
        assertTicketEquals(ticket, newTicket);

        return ticket.getId();
    }

    private void get(String ticketId) throws PluginException {
        Ticket newTicket = m_ticketer.get(ticketId);

        assertNotNull(newTicket);
        assertEquals(ticketId, newTicket.getId());
        assertEquals(Ticket.State.OPEN, newTicket.getState());
        assertTrue("Unexpected summary: " + newTicket.getSummary(), newTicket.getSummary().contains("This is the summary"));
        assertTrue("Unexpected description: " + newTicket.getDetails(), newTicket.getDetails().contains("details"));
    }

    private void update(String ticketId) throws PluginException, InterruptedException {

        String summary = "A Ticket at " + new Date();

        Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary(summary);
        ticket.setDetails("Ticket details for ticket: " + new Date());

        m_ticketer.saveOrUpdate(ticket);

        assertNotNull(ticket.getId());

        Ticket newTicket = m_ticketer.get(ticket.getId());

        assertNotNull(newTicket);
        assertTicketEquals(ticket, newTicket);

        newTicket.setState(Ticket.State.CLOSED);
        newTicket.setDetails("These details have changed");

        System.err.println("TicketId = " + newTicket.getId());

        m_ticketer.saveOrUpdate(newTicket);

        Thread.sleep(500);

        Ticket newerTicket = m_ticketer.get(newTicket.getId());

        assertTicketEquals(newTicket, newerTicket);
    }

    private static void assertTicketEquals(Ticket ticket, Ticket newTicket) {
        assertEquals(ticket.getId(), newTicket.getId());
        assertEquals(ticket.getState(), newTicket.getState());
        assertEquals(ticket.getSummary(), newTicket.getSummary());
    }
}
