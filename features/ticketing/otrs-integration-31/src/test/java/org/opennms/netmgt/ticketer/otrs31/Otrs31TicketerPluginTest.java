/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.otrs31;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Date;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This test assumes that you have a localhost OTRS service available so it is 
 * marked as @Ignore by default.
 */
@Ignore
public class Otrs31TicketerPluginTest {

    private static final Logger LOG = LoggerFactory.getLogger(Otrs31TicketerPluginTest.class);

    private static String DEFAULT_USER = "customer@localhost";

    private static String SUMMARY_MARKUP = "<p>Test Remove Markup</p>";
    
    private static String SUMMARY_NO_MARKUP = "Test Remove Markup";

    private static Otrs31TicketerPlugin otrsPlugin;

    private static Ticket s_ticket;
    
    
    @BeforeClass
    public static void setUpPlugin() throws Exception {
        System.setProperty("opennms.home", "src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");
        LOG.info("src" + File.separatorChar + "test" + File.separatorChar + "opennms-home");

        otrsPlugin = new Otrs31TicketerPlugin();
        s_ticket = new Ticket();
        s_ticket.setState(Ticket.State.OPEN);
        s_ticket.setSummary("Ticket Summary for ticket: " + new Date());
        s_ticket.setDetails("First Article for ticket: " + new Date());
        //s_ticket.setUser(DEFAULT_USER);

    }
    

    @After
    public void cleanUp() throws PluginException {
        if (s_ticket != null && s_ticket.getId() != null) {
            s_ticket = otrsPlugin.get(s_ticket.getId());
            s_ticket.setState(Ticket.State.CLOSED);
            otrsPlugin.saveOrUpdate(s_ticket);
        }

    }

    @Test
    public void testCreate() throws PluginException {
        otrsPlugin.saveOrUpdate(s_ticket);
        Ticket otrsTicket = otrsPlugin.get(s_ticket.getId());
        assertTicketEquals(s_ticket, otrsTicket);
    }

    @Test
    public void testOpenAndClose() throws PluginException {
        Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary("Testing Open and Close Summary: " + new Date());
        ticket.setDetails("Testing Open and Close Detail: " + new Date());
        //ticket.setUser(DEFAULT_USER);

        otrsPlugin.saveOrUpdate(ticket);
        Ticket initialOtrsTicket = otrsPlugin.get(ticket.getId());
        assertTicketEquals(ticket, initialOtrsTicket);
        ticket.setState(Ticket.State.CLOSED);
        otrsPlugin.saveOrUpdate(ticket);
        Ticket closedOtrsTicket = otrsPlugin.get(ticket.getId());
        assertTicketEquals(ticket, closedOtrsTicket);

    }
    
    @Test
    public void testRemoveMarkup() throws PluginException {

    	Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary(SUMMARY_MARKUP);
        ticket.setDetails("Testing Markup Removal from title: " + new Date());
        //ticket.setUser(DEFAULT_USER);

        otrsPlugin.saveOrUpdate(ticket);
        Ticket initialOtrsTicket = otrsPlugin.get(ticket.getId());
        assertEquals(initialOtrsTicket.getSummary(),SUMMARY_NO_MARKUP);
        ticket.setState(Ticket.State.CLOSED);
        otrsPlugin.saveOrUpdate(ticket);

    }

    private void assertTicketEquals(Ticket existing, Ticket retrieved) {
        assertEquals(existing.getId(), retrieved.getId());
        assertEquals(existing.getState(), retrieved.getState());
        // Do not test the User as we always override the user 
        // To ensure that its is a valid OTRS customer user.
        // assertEquals(existing.getUser(), retrieved.getUser());
        assertEquals(existing.getSummary(), retrieved.getSummary());
        //assertEquals(existing.getDetails(), retrieved.getDetails());
    }
    
}
