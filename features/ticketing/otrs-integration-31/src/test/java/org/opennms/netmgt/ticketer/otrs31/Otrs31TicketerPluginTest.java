/*******************************************************************************
 * This file is part of OpenNMS(R). Copyright (C) 2008-2013 The OpenNMS Group,
 * Inc. OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc. OpenNMS(R)
 * is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. OpenNMS(R) is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details. You should have received a copy of the GNU General Public
 * License along with OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ticketer.otrs31;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assume.assumeTrue;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.netmgt.ticketer.otrs.common.DefaultOtrsConfigDao;

public class Otrs31TicketerPluginTest {

    // defaults for ticket

    private static String DEFAULT_USER = "customer@localhost";

    DefaultOtrsConfigDao m_configDao;

    static Otrs31TicketerPlugin s_ticketer;

    static Ticket s_ticket;
    
    

    @BeforeClass
    public static void setUpPlugin() throws Exception {
        
        assumeTrue(Boolean.getBoolean("runOtrs31Tests"));

        System.setProperty("opennms.home", "src" + File.separatorChar
                + "test" + File.separatorChar + "opennms-home");

        System.out.println("src" + File.separatorChar + "test"
                + File.separatorChar + "opennms-home");

        s_ticketer = new Otrs31TicketerPlugin();

        s_ticket = new Ticket();
        s_ticket.setState(Ticket.State.OPEN);
        s_ticket.setSummary("Ticket Summary for ticket: " + new Date());
        s_ticket.setDetails("First Article for ticket: " + new Date());
        //s_ticket.setUser(DEFAULT_USER);

    }
    

    @Test
    public void testCreate() {

        try {
            s_ticketer.saveOrUpdate(s_ticket);
            Ticket otrsTicket = s_ticketer.get(s_ticket.getId());
            assertTicketEquals(s_ticket, otrsTicket);
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testOpenAndClose() throws InterruptedException {

        Ticket ticket = new Ticket();
        ticket.setState(Ticket.State.OPEN);
        ticket.setSummary("Testing Open and Close Summary: " + new Date());
        ticket.setDetails("Testing Open and Close Detail: " + new Date());
        //ticket.setUser(DEFAULT_USER);

        try {
            s_ticketer.saveOrUpdate(ticket);
            Ticket initialOtrsTicket = s_ticketer.get(ticket.getId());
            assertTicketEquals(ticket, initialOtrsTicket);
            ticket.setState(Ticket.State.CLOSED);
            s_ticketer.saveOrUpdate(ticket);
            Ticket closedOtrsTicket = s_ticketer.get(ticket.getId());
            assertTicketEquals(ticket, closedOtrsTicket);
        } catch (PluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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
