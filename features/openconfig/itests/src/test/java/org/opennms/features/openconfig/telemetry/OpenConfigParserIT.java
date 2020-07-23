/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.features.openconfig.telemetry;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.protocols.openconfig.parser.OpenConfigParser;
import org.opennms.netmgt.telemetry.stream.listeners.TelemetryStreamListener;

import com.codahale.metrics.MetricRegistry;

public class OpenConfigParserIT {

    private OpenConfigTestServer server;

    private MockAsyncDispatcher mockAsyncDispatcher = new MockAsyncDispatcher();

    @Before
    public void setup() throws IOException {
        server = new OpenConfigTestServer();
        server.start();
    }


    @Test
    public void testOpenConfigParsing() throws InterruptedException, UnknownHostException {
        Map<String, String> params = new HashMap<>();
        params.put("port", "50051");
        params.put("paths", "/interfaces");
        params.put("frequency", "5000");
        params.put("enabled", "${requisition:oc.enabled|true}");
        IpInterfaceDao ipInterfaceDao = Mockito.mock(IpInterfaceDao.class);
        OnmsNode onmsNode = Mockito.mock(OnmsNode.class);
        Mockito.when(onmsNode.getId()).thenReturn(1);
        OnmsIpInterface ipInterface = new OnmsIpInterface(InetAddress.getByName("127.0.0.1"), onmsNode);
        List<OnmsIpInterface> ipInterfaces = new ArrayList<>();
        ipInterfaces.add(ipInterface);
        Mockito.when(ipInterfaceDao.findInterfacesWithMetadata(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(ipInterfaces);

        ipInterfaceDao.save(new OnmsIpInterface(InetAddress.getByName("127.0.0.1"), new OnmsNode()));
        OpenConfigParser parser = new OpenConfigParser("openconfig-parser",
                params,
                mockAsyncDispatcher,
                new MockEntityProvider(),
                ipInterfaceDao, new OpenConfigClientFactory());
        TelemetryStreamListener telemetryStreamListener = new TelemetryStreamListener(
                "openconfig-stream-listener", parser, new MockEventIpcManager(), new MetricRegistry());
        telemetryStreamListener.start();
        await().atMost(10, TimeUnit.SECONDS).until(() -> mockAsyncDispatcher.getMessageList().size(), is(greaterThan(0)));

    }


    @After
    public void shutdown() {
        if (server != null) {
            server.stop();
        }
    }

}
