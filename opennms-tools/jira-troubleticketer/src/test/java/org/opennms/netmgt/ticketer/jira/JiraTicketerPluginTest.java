/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.jira;

import junit.framework.TestCase;
import org.opennms.api.integration.ticketing.Ticket;

import java.io.File;
import java.util.Date;

public class JiraTicketerPluginTest extends TestCase {

    JiraTicketerPlugin m_ticketer;

    @Override
    protected void setUp() throws Exception {

        System.setProperty("opennms.home", "src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");

        System.out.println("src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");

        m_ticketer = new JiraTicketerPlugin();
    }

    public void testSave() {

        Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary("This is the summary");
        ticket.setDetails("These are the details");

        m_ticketer.saveOrUpdate(ticket);

        assertNotNull(ticket.getId());

        Ticket newTicket = m_ticketer.get(ticket.getId());

        assertNotNull(newTicket);
        assertTicketEquals(ticket, newTicket);

    }

    public void testUpdate() throws Exception {

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

    private void assertTicketEquals(Ticket ticket, Ticket newTicket) {
        assertEquals(ticket.getId(), newTicket.getId());
        assertEquals(ticket.getState(), newTicket.getState());
        assertEquals(ticket.getSummary(), newTicket.getSummary());

        //TODO: Implement this later when we need 2 way retrieval of comments/details
        //assertEquals(ticket.getDetails(), newTicket.getDetails());
    }

    public void testGet() {

        //This may need to be changed ;-)
        String ticketId = "TST-12206";
        Ticket newTicket = m_ticketer.get(ticketId);

        assertNotNull(newTicket);
        assertEquals(ticketId, newTicket.getId());
        System.out.println(newTicket.getId() + ":" + newTicket.getSummary());
        assertTrue(newTicket.getSummary().startsWith("This is the summary"));

        //TODO: Implement this later when we need 2 way retrieval of comments/details
        //assertEquals("These are the details", newTicket.getDetails());

    }

}
