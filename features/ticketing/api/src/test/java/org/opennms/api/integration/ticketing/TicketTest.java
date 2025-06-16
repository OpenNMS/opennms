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
package org.opennms.api.integration.ticketing;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

public class TicketTest {

    @Test
    public void hasId() throws Exception {
        Ticket ticket = new Ticket();
        Assert.assertEquals(Boolean.FALSE, ticket.hasId());
        ticket.setId("");
        Assert.assertEquals(Boolean.FALSE, ticket.hasId());
        ticket.setId("test");
        Assert.assertEquals(Boolean.TRUE, ticket.hasId());
    }

    @Test
    public void hasAttributes() throws Exception {
        Ticket ticket = new Ticket();
        Assert.assertEquals(Boolean.FALSE, ticket.hasAttributes());
        ticket.setAttributes(new HashMap<>());
        Assert.assertEquals(Boolean.FALSE, ticket.hasAttributes());
        ticket.addAttribute("user", "ulf");
        Assert.assertEquals(Boolean.TRUE, ticket.hasAttributes());
    }

}