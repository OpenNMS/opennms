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
package org.opennms.netmgt.ticketer.jira;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.MapScope;
import org.opennms.core.mate.api.Scope;

@Ignore("These tests rely on the Jira system to be configured correctly (see jira.properties in src/test/resources/opennms-home/etc/jira.properties)")
public class JiraTicketerPluginTest {

    JiraTicketerPlugin m_ticketer;

    EntityScopeProvider entityScopeProvider;

    @Before
    public void setUp() throws Exception {
        final File opennmsHome = Paths.get("src", "test", "resources", "opennms-home").toFile();
        assertTrue(opennmsHome + " must exist.", opennmsHome.exists());
        System.setProperty("opennms.home", opennmsHome.getAbsolutePath());

        final Map<ContextKey, String> map = new HashMap<>();
        map.put(new ContextKey("scv", "jira:username"), "john");
        map.put(new ContextKey("scv", "jira:password"), "secret");
        final Scope mapScope = new MapScope(Scope.ScopeName.DEFAULT, map);

        entityScopeProvider = mock(EntityScopeProvider.class);
        when(entityScopeProvider.getScopeForScv()).thenReturn(mapScope);

        m_ticketer = new JiraTicketerPlugin(entityScopeProvider);
    }

    @Test
    public void verifyTooManyFiles() throws PluginException {
        JiraTicketerPlugin plugin = new JiraTicketerPlugin(entityScopeProvider);
        for (int i=0; i<500; i++) {
            System.out.print(i + ": ");
            Ticket ticket = plugin.get("NMS-8947");
            System.out.print(ticket.getSummary() + "\n");
        }
    }

    @Test
    public void canSaveGetAndUpdate() throws Exception {
        String ticketId = save();
        get(ticketId);
        update(ticketId);
    }

    @Test
    public void testMetadata() {
        assertEquals("john", JiraTicketerPlugin.getConfig(entityScopeProvider).getUsername());
        assertEquals("secret", JiraTicketerPlugin.getConfig(entityScopeProvider).getPassword());
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
