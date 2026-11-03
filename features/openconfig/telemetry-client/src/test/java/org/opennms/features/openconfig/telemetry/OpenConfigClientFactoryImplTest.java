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
package org.opennms.features.openconfig.telemetry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.opennms.features.openconfig.api.OpenConfigClient;

public class OpenConfigClientFactoryImplTest {

    private static final OpenConfigClient.Handler NOOP = new OpenConfigClient.Handler() {
        @Override
        public void accept(InetAddress host, Integer port, byte[] data) { }

        @Override
        public void onError(String error) { }
    };

    @Test(timeout = 15000)
    public void shutdownClosesEveryLiveClientAndRejectsNewOnes() throws Exception {
        OpenConfigClientFactoryImpl factory = new OpenConfigClientFactoryImpl();
        // Accepts TCP but never speaks gRPC, so every client stays in its connection attempt.
        try (ServerSocket stalled = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
            OpenConfigClientImpl first = create(factory, stalled.getLocalPort());
            OpenConfigClientImpl second = create(factory, stalled.getLocalPort());
            OpenConfigClientImpl third = create(factory, stalled.getLocalPort());
            first.subscribe(NOOP);
            second.subscribe(NOOP);
            assertEquals(3, factory.activeClients());

            // A client shutting itself down leaves the registry.
            third.shutdown();
            assertTrue(third.isClosed());
            assertEquals(2, factory.activeClients());

            factory.shutdown();
            factory.shutdown();   // idempotent
            assertTrue("Subscribed client was not closed", first.isClosed());
            assertTrue("Subscribed client was not closed", second.isClosed());
            assertEquals(0, factory.activeClients());

            assertThrows(IllegalStateException.class,
                    () -> factory.create(InetAddress.getByName("127.0.0.1"), params(stalled.getLocalPort())));
        }
    }

    private static OpenConfigClientImpl create(OpenConfigClientFactoryImpl factory, int port) throws Exception {
        return (OpenConfigClientImpl) factory.create(InetAddress.getByName("127.0.0.1"), params(port));
    }

    private static List<Map<String, String>> params(int port) {
        return List.of(Map.of("port", Integer.toString(port), "mode", "gnmi", "tls.enabled", "false",
                "paths", "/interfaces/interface/state/counters"));
    }
}
