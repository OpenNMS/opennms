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
package org.opennms.netmgt.poller.monitors;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

public class BgpSessionMonitorTest {

    private static final String RFC1269_BGP_PEER_STATE_OID = ".1.3.6.1.2.1.15.3.1.2";
    private static final String RFC1269_BGP_PEER_ADMIN_STATE_OID = ".1.3.6.1.2.1.15.3.1.3";
    private static final String RFC1269_BGP_PEER_REMOTEAS_OID = ".1.3.6.1.2.1.15.3.1.9";
    private static final String RFC1269_BGP_PEER_LAST_ERROR_OID = ".1.3.6.1.2.1.15.3.1.14";
    private static final String RFC1269_BGP_PEER_FSM_EST_TIME_OID = ".1.3.6.1.2.1.15.3.1.16";

    @Test
    public void testDefaultOidConstruction() {
        final Map<String, Object> params = new TreeMap<>();
        final BgpSessionMonitor.BgpOids bgpOids = new BgpSessionMonitor.BgpOids(params);
        Assert.assertEquals(RFC1269_BGP_PEER_STATE_OID, bgpOids.bgpPeerStateOid);
        Assert.assertEquals(RFC1269_BGP_PEER_ADMIN_STATE_OID, bgpOids.bgpPeerAdminStateOid);
        Assert.assertEquals(RFC1269_BGP_PEER_FSM_EST_TIME_OID, bgpOids.bgpPeerFsmEstTimeOid);
        Assert.assertEquals(RFC1269_BGP_PEER_REMOTEAS_OID, bgpOids.bgpPeerRemoteAsOid);
        Assert.assertEquals(RFC1269_BGP_PEER_LAST_ERROR_OID, bgpOids.bgpPeerLastErrorOid);
    }

    @Test
    public void testOverwriteOidConstruction() {
        final Map<String, Object> params = new TreeMap<>();
        params.put("bgpPeerStateOid", ".9.1");
        params.put("bgpPeerAdminStateOid", ".9.2");
        params.put("bgpPeerRemoteAsOid", ".9.3");
        params.put("bgpPeerLastErrorOid", ".9.4");
        params.put("bgpPeerFsmEstTimeOid", ".9.5");
        final BgpSessionMonitor.BgpOids bgpOids = new BgpSessionMonitor.BgpOids(params);
        Assert.assertEquals(".1.3.6.1.2.1.15", bgpOids.bgpBaseOid);
        Assert.assertEquals(".9.1", bgpOids.bgpPeerStateOid);
        Assert.assertEquals(".9.2", bgpOids.bgpPeerAdminStateOid);
        Assert.assertEquals(".9.3", bgpOids.bgpPeerRemoteAsOid);
        Assert.assertEquals(".9.4", bgpOids.bgpPeerLastErrorOid);
        Assert.assertEquals(".9.5", bgpOids.bgpPeerFsmEstTimeOid);
    }

    @Test
    public void testBaseOidConstruction() {
        final Map<String, Object> params = new TreeMap<>();
        params.put("bgpBaseOid", ".9");
        final BgpSessionMonitor.BgpOids bgpOids = new BgpSessionMonitor.BgpOids(params);
        Assert.assertEquals(".9", bgpOids.bgpBaseOid);
        Assert.assertEquals(".9.3.1.2", bgpOids.bgpPeerStateOid);
        Assert.assertEquals(".9.3.1.3", bgpOids.bgpPeerAdminStateOid);
        Assert.assertEquals(".9.3.1.9", bgpOids.bgpPeerRemoteAsOid);
        Assert.assertEquals(".9.3.1.14", bgpOids.bgpPeerLastErrorOid);
        Assert.assertEquals(".9.3.1.16", bgpOids.bgpPeerFsmEstTimeOid);
    }
}
