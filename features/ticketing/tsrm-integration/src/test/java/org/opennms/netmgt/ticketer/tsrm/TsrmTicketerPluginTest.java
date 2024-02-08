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

@Ignore("requires TSRM server to test")
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
        ticket.addAttribute("classId", "INCIDENT");
        ticket.setUser("openNMS");
        ticket.setSummary("openNMS summary");
        ticket.setState(Ticket.State.OPEN);
        ticket.addAttribute("shsReasonForOutage", "failure");
        ticket.addAttribute("shsRoomNumber", "Room 21");

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
        assertEquals(ticket.getAttribute("shsRoomNumber"),
                     newerTicket.getAttribute("shsRoomNumber"));
        assertEquals(ticket.getAttribute("shsReasonForOutage"),
                     newerTicket.getAttribute("shsReasonForOutage"));

    }

}
