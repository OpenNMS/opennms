/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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