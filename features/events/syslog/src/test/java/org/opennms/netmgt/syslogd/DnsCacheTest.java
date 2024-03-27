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
package org.opennms.netmgt.syslogd;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.mock.MockDistPollerDao;
import org.opennms.netmgt.provision.LocationAwareDnsLookupClient;
import org.opennms.netmgt.xml.event.Event;

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
                    MockDistPollerDao.DEFAULT_DIST_POLLER_ID,
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
