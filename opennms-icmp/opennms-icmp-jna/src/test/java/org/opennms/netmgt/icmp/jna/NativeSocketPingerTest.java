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
package org.opennms.netmgt.icmp.jna;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.icmp.IcmpMessengerIOException;

public class NativeSocketPingerTest {

    /** What the messenger throws when neither socket opens. */
    private static IOException unavailable(final Throwable cause) {
        return new IcmpMessengerIOException("IPv4 and IPv6 are not available.", cause);
    }

    /** A pinger whose sockets fail to open with the given error. */
    private static NativeSocketPinger pingerFailingWith(final String socketError) {
        return new NativeSocketPinger(SocketBackend.FFM) {
            @Override
            JnaIcmpMessenger openMessenger() throws Exception {
                throw unavailable(new IOException(socketError));
            }
        };
    }

    private static long hintCount() {
        return Arrays.stream(MockLogAppender.getEvents())
                .filter(e -> e.getLevel() == Level.WARN && e.getMessage().contains("Permission error received"))
                .count();
    }

    @Test
    public void findsTheFfmPermissionErrorBehindTheWrapper() {
        final Throwable cause = new IOException("socket: Operation not permitted (errno 1)");
        assertSame(cause, NativeSocketPinger.permissionError(unavailable(cause)));
    }

    @Test
    public void findsTheFfmPermissionErrorInAnotherLocale() {
        final Throwable cause = new IOException("socket: Keine Berechtigung (errno 13)");
        assertSame(cause, NativeSocketPinger.permissionError(unavailable(cause)));
    }

    @Test
    public void findsTheJnaPermissionErrorInAnotherLocale() {
        final Throwable cause = new RuntimeException("[1] Die Operation ist nicht erlaubt");
        assertSame(cause, NativeSocketPinger.permissionError(unavailable(cause)));
    }

    @Test
    public void otherErrorsAreNotPermissionErrors() {
        assertNull(NativeSocketPinger.permissionError(unavailable(null)));
        assertNull(NativeSocketPinger.permissionError(unavailable(new IOException())));
        assertNull(NativeSocketPinger.permissionError(unavailable(new IOException("socket: Address family not supported by protocol (errno 97)"))));
        assertNull(NativeSocketPinger.permissionError(unavailable(new IOException("socket: Resource temporarily unavailable (errno 11)"))));
        assertNull(NativeSocketPinger.permissionError(unavailable(new RuntimeException("[11] Resource temporarily unavailable"))));
    }

    @Test
    public void refusedSocketLogsTheHintOncePerJvm() {
        MockLogAppender.setupLogging();
        assertThrows(IOException.class, pingerFailingWith("socket: Address family not supported by protocol (errno 97)")::initialize4);
        assertEquals(0, hintCount());

        final NativeSocketPinger refused = pingerFailingWith("socket: Operation not permitted (errno 1)");
        assertThrows(IOException.class, refused::initialize4);
        assertThrows(IOException.class, refused::initialize4);
        assertEquals(1, hintCount());
    }
}
