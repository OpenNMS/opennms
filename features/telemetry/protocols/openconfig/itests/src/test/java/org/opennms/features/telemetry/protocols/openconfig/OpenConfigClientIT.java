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
package org.opennms.features.telemetry.protocols.openconfig;

import static org.awaitility.Awaitility.await;

import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.features.openconfig.proto.jti.Telemetry;
import org.opennms.features.openconfig.telemetry.OpenConfigClientImpl;
import org.opennms.netmgt.telemetry.config.model.Parameter;

import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;

public class OpenConfigClientIT {

    private final List<Telemetry.OpenConfigData> jtiData = new ArrayList<>();
    private final List<Gnmi.SubscribeResponse> gnmiData = new ArrayList<>();
    private OpenConfigTestServer server;
    private int port;

    @Before
    public void setup() throws IOException {
        this.port = OpenConfigTestServer.getAvailablePort(new AtomicInteger(50052), 51000);
        server = new OpenConfigTestServer(port);
        server.start();
    }

    @Test
    public void testOpenConfigJti() throws Exception {

        List<Map<String, String>> params = getParams(true);
        InetAddress host = InetAddress.getLocalHost();
        OpenConfigClientImpl openConfigClient = new OpenConfigClientImpl(host, params);
        openConfigClient.subscribe(new DataHandler(true));
        // Wait till at least 2 data streams received.
        await().atMost(15, TimeUnit.SECONDS).until(jtiData::size, is(greaterThan(1)));

    }

    @Test
    public void testOpenConfigJTIAfterClientShutdown_collectionBased() throws Exception {
        List<Map<String, String>> params = getParams(true);
        InetAddress host = InetAddress.getLocalHost();
        OpenConfigClientImpl openConfigClient = new OpenConfigClientImpl(host, params);
        openConfigClient.subscribe(new DataHandler(true));
        // Wait till at least 2 data streams received.
        await().atMost(15, TimeUnit.SECONDS).until(jtiData::size, is(greaterThan(1)));

        openConfigClient.shutdown();

        final int beforeShutdown = gnmiData.size();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (gnmiData.size() > beforeShutdown) {
                fail("Received data after shutdown: before=" + beforeShutdown + " now=" + gnmiData.size());
            }
        }

    }



    @Test
    public void testOpenConfigGnmi() throws Exception {

        List<Map<String, String>> params = getParams(false);
        InetAddress host = InetAddress.getLocalHost();
        OpenConfigClientImpl openConfigClient = new OpenConfigClientImpl(host, params);
        openConfigClient.subscribe(new DataHandler(false));
        // Wait till at least 2 data streams received.
        await().atMost(15, TimeUnit.SECONDS).until(gnmiData::size, is(greaterThan(1)));

    }

    @Test
    public void testOpenConfigGnmiAfterClientShutdown_collectionBased() throws Exception {
        List<Map<String, String>> params = getParams(false);
        InetAddress host = InetAddress.getLocalHost();
        OpenConfigClientImpl openConfigClient = new OpenConfigClientImpl(host, params);

        openConfigClient.subscribe(new DataHandler(false));

        // Wait until we have at least 2 messages
        await().atMost(15, TimeUnit.SECONDS).until(gnmiData::size, greaterThan(1));

        openConfigClient.shutdown();

        final int beforeShutdown = gnmiData.size();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (gnmiData.size() > beforeShutdown) {
                fail("Received data after shutdown: before=" + beforeShutdown + " now=" + gnmiData.size());
            }
        }

    }



    @Test
    public void testOpenConfigScheduling() throws Exception {
        jtiData.clear();
        List<Map<String, String>> params = getParams(true);
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

    private List<Map<String, String>> getParams(boolean jti) {
        List<Map<String, String>> params = new ArrayList<>();
        List<Parameter> parameterList = new ArrayList<>();
        if(jti) {
            parameterList.add(new Parameter("mode", "jti"));
        }
        String port = Integer.toString(this.port);
        parameterList.add(new Parameter("port", port));
        parameterList.add(new Parameter("group1", "frequency", "2000"));
        parameterList.add(new Parameter("group1", "paths", "/interfaces"));
        parameterList.add(new Parameter("group1", "interval", "3"));
        parameterList.add(new Parameter("group2", "frequency", "3000"));
        parameterList.add(new Parameter("group2", "paths",
                "/network-instances/network-instance[instance-name='master']"));
        parameterList.add(new Parameter("group3", "paths", "/protocols/protocol/bgp"));
        parameterList.add(new Parameter("group3", "frequency", "4000"));
        Map<String, Map<String, String>> parmsWithGroup = parameterList.stream()
                .filter(parameter -> !Strings.isNullOrEmpty(parameter.getGroup()))
                .collect(Collectors.groupingBy(Parameter::getGroup, Collectors.toMap(Parameter::getKey, Parameter::getValue)));

        Map<String, String> parmsWithoutGroup = parameterList.stream()
                .filter(parameter -> Strings.isNullOrEmpty(parameter.getGroup()))
                .collect(Collectors.toMap(
                        Parameter::getKey,
                        Parameter::getValue
                ));
        parmsWithGroup.forEach((group, map) -> {
            params.add(map);
        });
        params.add(parmsWithoutGroup);
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
