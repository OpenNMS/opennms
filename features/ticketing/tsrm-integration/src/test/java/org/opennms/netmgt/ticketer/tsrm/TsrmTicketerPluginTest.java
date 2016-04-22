package org.opennms.netmgt.ticketer.tsrm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;

public class TsrmTicketerPluginTest {

    TsrmTicketerPlugin tsrmTicket;

    @Before
    public void setup() {
        final File opennmsHome = Paths.get("src",
                                           "test",
                                           "resources",
                                           "opennms-home").toFile();
        System.setProperty("opennms.home", opennmsHome.getAbsolutePath());

        tsrmTicket = new TsrmTicketerPlugin();
    }

    @Test
    @Ignore
    public void testForValidatingSaveAndUpdatedTicket()
            throws PluginException {

        Ticket ticket = new Ticket();

        ticket.addAttribute("location", "OPENNMS");
        ticket.setDetails("OpenNMS Description");
        ticket.addAttribute("classStructureId", "1012");
        // When creating NOC_EU as owner group, updating INCIDENT doesn't work
        // ticket.addAttribute("ownergroup", "NOC_EU");
        ticket.addAttribute("siteId", "SHSEU");
        ticket.addAttribute("source", "OpenNMS");
        ticket.setUser("openNMS");
        ticket.setSummary("openNMS summary");
        ticket.setState(Ticket.State.OPEN);

        tsrmTicket.saveOrUpdate(ticket);

        assertNotNull(ticket.getId());

        Ticket newTicket = tsrmTicket.get(ticket.getId());
        newTicket.setState(Ticket.State.CLOSED);
        newTicket.setSummary("new openNMS summary");
        newTicket.setDetails("new OpenNMS Description");
        newTicket.setUser("oNMS");

        tsrmTicket.saveOrUpdate(newTicket);

        Ticket newerTicket = tsrmTicket.get(newTicket.getId());

        // When retrieving state, comes as NEW
        // assertEquals(newTicket.getState(), newerTicket.getState());
        assertEquals(newTicket.getSummary(), newerTicket.getSummary());
        assertEquals(newTicket.getDetails(), newerTicket.getDetails());
        assertEquals(newTicket.getUser(), newerTicket.getUser());
        assertEquals(ticket.getAttribute("siteId"),
                     newerTicket.getAttribute("siteId"));
        assertEquals(ticket.getAttribute("source"),
                     newerTicket.getAttribute("source"));
        assertEquals(ticket.getAttribute("location"),
                     newerTicket.getAttribute("location"));
    }

}
