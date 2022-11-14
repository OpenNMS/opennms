/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
