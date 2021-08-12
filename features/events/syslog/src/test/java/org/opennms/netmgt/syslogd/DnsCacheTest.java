/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;
import org.opennms.netmgt.xml.event.Event;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DnsCacheTest {

    private LocationAwareDnsLookupClient locationAwareDnsLookupClient;
    private Cache<HostNameWithLocationKey, String> cache;
    private static final SyslogConfigBean radixConfig = new SyslogConfigBean();

    @Before
    public void setup() {
        radixConfig.setParser("org.opennms.netmgt.syslogd.RadixTreeSyslogParser");
        radixConfig.setDiscardUei("DISCARD-MATCHING-MESSAGES");
        locationAwareDnsLookupClient = Mockito.mock(LocationAwareDnsLookupClient.class);
        Mockito.when(locationAwareDnsLookupClient.lookup(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(CompletableFuture.completedFuture("127.0.0.1"));
    }

    @Test
    public void testDnsCache() {
        // Set cache size to zero there by disabling cache.
        cache = CacheBuilder.newBuilder().maximumSize(0).build();
        String syslogMessage = "<34>1 2010-08-19T22:14:15.000Z " + "NotAHost" + " - - - - \uFEFFfoo0: load test 0 on tty1\0";
        // Send syslogMessage twice and lookup should be called twice.
        parseSyslog("testDnsCache", radixConfig, syslogMessage, new Date(), cache);
        parseSyslog("testDnsCache", radixConfig, syslogMessage, new Date(), cache);
        Mockito.verify(locationAwareDnsLookupClient, Mockito.times(2)).lookup(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        // Set cache size to 10 now.
        cache = CacheBuilder.newBuilder().maximumSize(10).build();
        Mockito.clearInvocations(locationAwareDnsLookupClient);
        // Send syslogMessage twice but lookup should be called only once.
        parseSyslog("testDnsCache", radixConfig, syslogMessage, new Date(), cache);
        parseSyslog("testDnsCache", radixConfig, syslogMessage, new Date(), cache);
        Mockito.verify(locationAwareDnsLookupClient, Mockito.times(1)).lookup(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }


    private Event parseSyslog(final String name, final SyslogdConfig config, final String syslog, Date receivedTimestamp, Cache<HostNameWithLocationKey, String> cache) {
        try {
            ConvertToEvent convert = new ConvertToEvent(
                    DistPollerDao.DEFAULT_DIST_POLLER_ID,
                    "MINION",
                    InetAddressUtils.ONE_TWENTY_SEVEN,
                    9999,
                    SyslogdTestUtils.toByteBuffer(syslog),
                    receivedTimestamp,
                    config,
                    locationAwareDnsLookupClient, cache);
            Event event = convert.getEvent();
            return event;

        } catch (MessageDiscardedException e) {
            return null;
        }
    }
}
