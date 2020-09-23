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

package org.opennms.features.telemetry.protocols.openconfig;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.features.openconfig.proto.jti.Telemetry;
import org.opennms.features.openconfig.telemetry.OpenConfigClientImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class OpenConfigClientIT {

    private final List<Telemetry.OpenConfigData> jtiData = new ArrayList<>();
    private final List<Gnmi.SubscribeResponse> gnmiData = new ArrayList<>();
    private OpenConfigTestServer server;

    @Before
    public void setup() throws IOException {
        server = new OpenConfigTestServer();
        server.start();
    }

    @Test
    public void testOpenConfigJti() throws Exception {

        Map<String, String> params = getParams(true);
        InetAddress host = InetAddress.getLocalHost();
        OpenConfigClientImpl openConfigClient = new OpenConfigClientImpl(host, params);
        openConfigClient.subscribe(new DataHandler(true));
        // Wait till at least 2 data streams received.
        await().atMost(15, TimeUnit.SECONDS).until(jtiData::size, is(greaterThan(1)));

    }

    @Test
    public void testOpenConfigGnmi() throws Exception {

        Map<String, String> params = getParams(false);
        InetAddress host = InetAddress.getLocalHost();
        OpenConfigClientImpl openConfigClient = new OpenConfigClientImpl(host, params);
        openConfigClient.subscribe(new DataHandler(false));
        // Wait till at least 2 data streams received.
        await().atMost(15, TimeUnit.SECONDS).until(gnmiData::size, is(greaterThan(1)));

    }

    @Test
    public void testOpenConfigScheduling() throws Exception {
        jtiData.clear();
        Map<String, String> params = getParams(true);
        InetAddress host = InetAddress.getLocalHost();
        server.setErrorStream();
        OpenConfigClientImpl openConfigClient = new OpenConfigClientImpl(host, params);
        openConfigClient.subscribe(new DataHandler(true));
        // Wait till at least 2 data streams received.
        await().atMost(15, TimeUnit.SECONDS).until(jtiData::size, is(greaterThan(1)));
        // Stop the server and see if client can make a connection
        server.stop();
        jtiData.clear();
        server.start();
        // Wait till at least 2 data streams received.
        await().atMost(15, TimeUnit.SECONDS).until(jtiData::size, is(greaterThan(1)));
    }

    private Map<String, String> getParams(boolean jti) {
        Map<String, String> params = new HashMap<>();
        if(jti) {
            params.put("mode", "jti");
        }
        params.put("port", "50052");
        params.put("paths", "/interfaces," +
                "/network-instances/network-instance[instance-name='master']," +
                "/protocols/protocol/bgp");
        params.put("frequency", "2000");
        params.put("interval", "3");
        return params;
    }

    private class DataHandler implements OpenConfigClient.Handler {

        private boolean jti;

        public DataHandler(boolean jti) {
            this.jti = jti;
        }

        @Override
        public void accept(InetAddress host, Integer port, byte[] data) {
            try {
                if(jti) {
                    Telemetry.OpenConfigData ocData = Telemetry.OpenConfigData.parseFrom(data);
                    jtiData.add(ocData);
                } else {
                    Gnmi.SubscribeResponse subscribeResponse = Gnmi.SubscribeResponse.parseFrom(data);
                    gnmiData.add(subscribeResponse);
                }
            } catch (InvalidProtocolBufferException e) {

            }
        }

        @Override
        public void onError(String error) {

        }
    }

    @After
    public void shutdown() {
        server.stop();
    }


}
