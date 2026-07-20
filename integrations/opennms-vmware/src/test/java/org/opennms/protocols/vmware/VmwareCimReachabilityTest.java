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
package org.opennms.protocols.vmware;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.ConnectException;
import java.net.ServerSocket;

import org.junit.Test;

import com.vmware.vim25.HostServiceTicket;
import com.vmware.vim25.mo.HostSystem;

/**
 * Verifies that {@link VmwareViJavaAccess#checkCimReachable(String, int)} bounds the
 * connection to a host's CIM service so that an unreachable or firewalled CIM port
 * fails fast instead of blocking the calling thread.
 */
public class VmwareCimReachabilityTest {

    private final VmwareViJavaAccess access = new VmwareViJavaAccess("host", "user", "pass");

    /**
     * A closed local port should be rejected (connection refused) promptly.
     */
    @Test
    public void failsWhenConnectionRefused() throws Exception {
        final int closedPort;
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            closedPort = serverSocket.getLocalPort();
        }
        // The server socket is now closed, so nothing is listening on closedPort.

        final long start = System.currentTimeMillis();
        try {
            access.checkCimReachable("https://127.0.0.1:" + closedPort, 5000);
            fail("expected a ConnectException for a closed CIM port");
        } catch (ConnectException expected) {
            final long elapsed = System.currentTimeMillis() - start;
            assertTrue("connection refused should be reported promptly, took " + elapsed + " ms", elapsed < 5000);
        }
    }

    /**
     * An unroutable host (RFC 5737 TEST-NET-1) must not block longer than the configured
     * timeout -- the SBLIM client would otherwise block for the OS connect timeout.
     */
    @Test
    public void boundsConnectForUnreachableHost() {
        final int timeout = 1000;
        final long start = System.currentTimeMillis();
        try {
            access.checkCimReachable("https://192.0.2.1:5989", timeout);
            fail("expected a ConnectException for an unreachable CIM host");
        } catch (ConnectException expected) {
            final long elapsed = System.currentTimeMillis() - start;
            assertTrue("connect should be bounded by the timeout, took " + elapsed + " ms",
                    elapsed < timeout + 4000);
        }
    }

    /**
     * A malformed agent address is left for the CIM client to surface (no exception here).
     */
    @Test
    public void ignoresMalformedAddress() throws Exception {
        access.checkCimReachable("not-a-url", 1000);
    }

    /**
     * Exercises the full {@code queryCimObjects} path with a mocked vCenter ticket (as a real
     * vCenter would mint) but an unreachable host CIM port. The query must fail within
     * cimTimeout rather than blocking on the SBLIM client's unbounded connect, and it must
     * not reach the CIM client at all.
     */
    @Test
    public void queryCimObjectsBoundsUnreachableCimConnect() throws Exception {
        access.setCimTimeout(800);

        final HostServiceTicket ticket = new HostServiceTicket();
        ticket.setSessionId("sessionId");
        final HostSystem hostSystem = mock(HostSystem.class);
        when(hostSystem.acquireCimServicesTicket()).thenReturn(ticket);

        final long start = System.currentTimeMillis();
        try {
            // 192.0.2.1 is RFC 5737 TEST-NET-1 (unroutable).
            access.queryCimObjects(hostSystem, "CIM_NumericSensor", "192.0.2.1");
            fail("expected a ConnectException for an unreachable CIM host");
        } catch (ConnectException expected) {
            final long elapsed = System.currentTimeMillis() - start;
            assertTrue("queryCimObjects should be bounded by cimTimeout, took " + elapsed + " ms",
                    elapsed < 5000);
        }
    }

    /**
     * Each call with an explicit address must probe that address, not a value cached from an
     * earlier call. The importer relies on this to try each of a host's interface addresses
     * in turn; caching the first one would wrongly mark a host with a reachable secondary
     * address as unreachable.
     */
    @Test
    public void queryCimObjectsUsesEachExplicitAddress() throws Exception {
        access.setCimTimeout(800);
        final HostServiceTicket ticket = new HostServiceTicket();
        ticket.setSessionId("sessionId");
        final HostSystem hostSystem = mock(HostSystem.class);
        when(hostSystem.acquireCimServicesTicket()).thenReturn(ticket);

        try {
            access.queryCimObjects(hostSystem, "CIM_NumericSensor", "192.0.2.1");
            fail("expected a ConnectException");
        } catch (ConnectException expected) {
            assertTrue("first probe should report 192.0.2.1: " + expected.getMessage(),
                    expected.getMessage().contains("192.0.2.1"));
        }

        try {
            access.queryCimObjects(hostSystem, "CIM_NumericSensor", "192.0.2.2");
            fail("expected a ConnectException");
        } catch (ConnectException expected) {
            assertTrue("second probe must use the supplied address, not a cached one: " + expected.getMessage(),
                    expected.getMessage().contains("192.0.2.2"));
        }
    }

    /**
     * An IPv6 host literal must be bracketed so the URL is a valid authority and the address is
     * actually probed -- an unbracketed IPv6 URL parses to a null host, so the probe would
     * silently no-op and leave the unbounded connect in place.
     */
    @Test
    public void queryCimObjectsProbesIpv6Address() throws Exception {
        access.setCimTimeout(800);
        final HostServiceTicket ticket = new HostServiceTicket();
        ticket.setSessionId("sessionId");
        final HostSystem hostSystem = mock(HostSystem.class);
        when(hostSystem.acquireCimServicesTicket()).thenReturn(ticket);

        try {
            // 2001:db8::5 is RFC 3849 documentation space (unroutable).
            access.queryCimObjects(hostSystem, "CIM_NumericSensor", "2001:db8::5");
            fail("expected a ConnectException for an unreachable IPv6 CIM host");
        } catch (ConnectException expected) {
            assertTrue("the IPv6 address must be probed, not skipped: " + expected.getMessage(),
                    expected.getMessage().contains("2001:db8::5"));
        }
    }

    /**
     * A timeout of 0 is an infinite Socket.connect timeout; it must be clamped so the probe
     * returns rather than blocking indefinitely.
     */
    @Test(timeout = 20000)
    public void clampsZeroTimeout() throws Exception {
        final long start = System.currentTimeMillis();
        try {
            access.checkCimReachable("https://192.0.2.1:5989", 0);
            fail("expected a ConnectException");
        } catch (ConnectException expected) {
            final long elapsed = System.currentTimeMillis() - start;
            assertTrue("a zero timeout must be clamped, took " + elapsed + " ms",
                    elapsed < VmwareViJavaAccess.DEFAULT_TIMEOUT + 5000);
        }
    }

    /**
     * A negative timeout would make Socket.connect throw IllegalArgumentException; it must be
     * clamped and surface as a normal connect failure instead.
     */
    @Test(timeout = 20000)
    public void clampsNegativeTimeout() throws Exception {
        try {
            access.checkCimReachable("https://192.0.2.1:5989", -1);
            fail("expected a ConnectException");
        } catch (ConnectException expected) {
            // ok: clamped to the default and reported as a connect failure, not IllegalArgumentException
        }
    }

    /**
     * An address that URI parsing rejects must be swallowed (left for the CIM client) rather
     * than propagated as an IllegalArgumentException.
     */
    @Test
    public void ignoresUnparseableAddress() throws Exception {
        access.checkCimReachable("https://bad host:5989", 1000);
    }

    /**
     * Non-positive timeouts must be clamped: this guards both the connect probe and the read
     * timeout (setHttpTimeOut), since 0 is an infinite timeout in both and a negative value is
     * rejected outright by Socket.connect.
     */
    @Test
    public void boundedTimeoutClampsNonPositive() {
        assertEquals(VmwareViJavaAccess.DEFAULT_TIMEOUT, VmwareViJavaAccess.boundedTimeout(0));
        assertEquals(VmwareViJavaAccess.DEFAULT_TIMEOUT, VmwareViJavaAccess.boundedTimeout(-5));
        assertEquals(800, VmwareViJavaAccess.boundedTimeout(800));
    }
}
