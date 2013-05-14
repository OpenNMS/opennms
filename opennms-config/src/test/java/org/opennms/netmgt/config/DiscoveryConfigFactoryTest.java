/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.model.discovery.IPPollAddress;

public class DiscoveryConfigFactoryTest {
    @Test
    public void testAddToSpecificsFromURLViaURL() throws Exception {
        final List<IPPollAddress> specifics = new ArrayList<IPPollAddress>();
        final URL in = this.getClass().getResource("validDiscoveryIncludeFile.txt");
        final long timeout = 100;
        final int retries = 1;
        DiscoveryConfigFactory.addToSpecificsFromURL(specifics, in.toString(), timeout, retries);
        assertEquals(8, specifics.size());
        assertEquals("127.0.0.1", InetAddressUtils.str(specifics.get(0).getAddress()));
        assertEquals("10.1.1.1", InetAddressUtils.str(specifics.get(1).getAddress()));
        assertEquals("10.2.1.1", InetAddressUtils.str(specifics.get(2).getAddress()));
        assertEquals("8.8.8.8", InetAddressUtils.str(specifics.get(3).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccc", InetAddressUtils.str(specifics.get(4).getAddress()));
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", InetAddressUtils.str(specifics.get(5).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccd", InetAddressUtils.str(specifics.get(6).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccc", InetAddressUtils.str(specifics.get(7).getAddress()));
    }
    
    @Test
    public void testAddToSpecificsFromURLViaStream() throws Exception {
        final List<IPPollAddress> specifics = new ArrayList<IPPollAddress>();
        final InputStream in = this.getClass().getResourceAsStream("validDiscoveryIncludeFile.txt");
        final long timeout = 100;
        final int retries = 1;
        DiscoveryConfigFactory.addToSpecificsFromURL(specifics, in, timeout, retries);
        assertEquals(8, specifics.size());
        assertEquals("127.0.0.1", InetAddressUtils.str(specifics.get(0).getAddress()));
        assertEquals("10.1.1.1", InetAddressUtils.str(specifics.get(1).getAddress()));
        assertEquals("10.2.1.1", InetAddressUtils.str(specifics.get(2).getAddress()));
        assertEquals("8.8.8.8", InetAddressUtils.str(specifics.get(3).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccc", InetAddressUtils.str(specifics.get(4).getAddress()));
        assertEquals("0000:0000:0000:0000:0000:0000:0000:0001", InetAddressUtils.str(specifics.get(5).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccd", InetAddressUtils.str(specifics.get(6).getAddress()));
        assertEquals("fe80:0000:0000:0000:ffff:eeee:dddd:cccc", InetAddressUtils.str(specifics.get(7).getAddress()));
    }
    
    @Test
    public void testMultipleExcludes() throws Exception {
        final DiscoveryConfigFactory factory = new DiscoveryConfigFactory() {
            @Override
            public void saveConfiguration(final DiscoveryConfiguration configuration) throws MarshalException, ValidationException, IOException {}
            @Override
            public synchronized DiscoveryConfiguration getConfiguration() {
                final DiscoveryConfiguration conf = new DiscoveryConfiguration();

                IncludeRange ir = new IncludeRange();
                ir.setBegin("192.168.0.1");
                ir.setEnd("192.168.0.254");
                conf.addIncludeRange(ir);

                ir = new IncludeRange();
                ir.setBegin("192.168.2.1");
                ir.setEnd("192.168.2.255");
                conf.addIncludeRange(ir);

                Specific s = new Specific();
                s.setContent("192.168.1.1");
                conf.addSpecific(s);

                s = new Specific();
                s.setContent("192.168.4.1");
                conf.addSpecific(s);

                ExcludeRange er = new ExcludeRange();
                er.setBegin("192.168.0.100");
                er.setEnd("192.168.0.150");
                conf.addExcludeRange(er);

                er = new ExcludeRange();
                er.setBegin("192.168.2.200");
                er.setEnd("192.168.4.254");
                conf.addExcludeRange(er);

                return conf;
            }
        };

        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.1")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.2")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.99")));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.100")));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.140")));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.150")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.151")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.1.1")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.2.1")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.2.100")));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.2.200")));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.2.220")));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.2.255")));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.4.1")));

    }
    @Test
    public void testSingleIPExclude() throws Exception {
        final DiscoveryConfigFactory factory = new DiscoveryConfigFactory() {
            @Override
            public void saveConfiguration(final DiscoveryConfiguration configuration) throws MarshalException, ValidationException, IOException {}
            @Override
            public synchronized DiscoveryConfiguration getConfiguration() {
                final DiscoveryConfiguration conf = new DiscoveryConfiguration();

                IncludeRange ir = new IncludeRange();
                ir.setBegin("192.168.0.1");
                ir.setEnd("192.168.0.254");
                conf.addIncludeRange(ir);

                ExcludeRange er = new ExcludeRange();
                er.setBegin("192.168.0.100");
                er.setEnd("192.168.0.100");
                conf.addExcludeRange(er);

                return conf;
            }
        };

        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.1")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.2")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.99")));
        assertTrue(factory.isExcluded(InetAddressUtils.addr("192.168.0.100")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.101")));
        assertFalse(factory.isExcluded(InetAddressUtils.addr("192.168.0.151")));
    }
}
