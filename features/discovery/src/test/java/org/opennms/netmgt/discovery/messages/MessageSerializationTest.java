/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.discovery.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Map;

import org.junit.Test;
import org.opennms.netmgt.discovery.messages.DiscoveryResults;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

public class MessageSerializationTest {

    /*
    @Test
    public void canSerializeJob() throws IOException {
        IPPollRange range = new IPPollRange("4.2.2.2", "4.2.2.2", 1, 1);
        List<IPPollRange> ranges = Lists.newArrayList(range);
        DiscoveryJob job = new DiscoveryJob(ranges, "x", "y");
        serialize(job);
    }
    */

    @Test
    public void canSerializeResults() throws IOException {
        Map<InetAddress, Long> responses = Maps.newConcurrentMap();
        responses.put(InetAddress.getByName("4.2.2.2"), 1L);
        DiscoveryResults results = new DiscoveryResults(responses, "x", "y");
        serialize(results);
    }

    private static void serialize(Object o) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new ByteArrayOutputStream());
            out.writeObject(o);
            out.close();
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }
}
