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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.proto.jti.Telemetry;

import com.google.protobuf.InvalidProtocolBufferException;

public class OpenConfigClientIT {

    private final List<Telemetry.OpenConfigData> openConfigData = new ArrayList<>();
    private OpenConfigTestServer server;

    @Before
    public void setup() throws IOException {
        server = new OpenConfigTestServer();
        server.start();
    }

    @Test
    public void testOpenConfigJti() throws Exception {

        Map<String, String> params = getParams();
        InetAddress host = InetAddress.getLocalHost();
        OpenConfigClientImpl openConfigClient = new OpenConfigClientImpl(host, params);
        openConfigClient.subscribe(new DataHandler());
        await().atMost(10, TimeUnit.SECONDS).until(openConfigData::size, is(greaterThan(0)));

    }

    @Test
    public void testOpenConfigScheduling() throws Exception {
        Map<String, String> params = getParams();
        InetAddress host = InetAddress.getLocalHost();
        server.setErrorStream();
        OpenConfigClientImpl openConfigClient = new OpenConfigClientImpl(host, params);
        openConfigClient.subscribe(new DataHandler());
        await().atMost(30, TimeUnit.SECONDS).until(openConfigData::size, is(greaterThan(0)));
    }

    private Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("port", "50051");
        params.put("paths", "/interfaces," +
                "/network-instances/network-instance[instance-name='master']," +
                "/protocols/protocol/bgp");
        params.put("frequency", "5000");
        return params;
    }

    private class DataHandler implements OpenConfigClient.Handler {

        @Override
        public void accept(InetAddress host, Integer port, byte[] data) {
            try {
                Telemetry.OpenConfigData ocData = Telemetry.OpenConfigData.parseFrom(data);
                openConfigData.add(ocData);
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
