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

import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import org.opennms.jicmp.jna.NativeDatagramSocket;

import com.sun.jna.LastErrorException;

public class SocketBackendTest {

    private NativeDatagramSocket m_socket;

    @After
    public void tearDown() {
        if (m_socket != null) m_socket.close();
    }

    private NativeDatagramSocket open(final SocketBackend backend) throws Exception {
        try {
            return backend.open(NativeDatagramSocket.AF_INET, NativeDatagramSocket.IPPROTO_ICMP, 0x4f4e);
        } catch (final Exception e) {
            // Skip only when the kernel refuses the socket. Any other failure is a bug.
            if (!notPermitted(e)) {
                throw e;
            }
            Assume.assumeNoException("ICMP sockets not permitted here", e);
            return null;
        }
    }

    /** True for EPERM (1) or EACCES (13), from the JNA exception or the FFM "(errno N)" message. */
    private static boolean notPermitted(final Exception e) {
        if (e instanceof LastErrorException) {
            final int errno = ((LastErrorException) e).getErrorCode();
            return errno == 1 || errno == 13;
        }
        final String message = String.valueOf(e.getMessage());
        return message.endsWith("(errno 1)") || message.endsWith("(errno 13)");
    }

    @Test
    public void ffmOpensTheFfmSocket() throws Exception {
        m_socket = open(SocketBackend.FFM);
        assertEquals("org.opennms.jicmp.ffm.FfmNativeDatagramSocket", m_socket.getClass().getName());
    }

    @Test
    @SuppressWarnings("removal")
    public void jnaOpensAJnaSocket() throws Exception {
        m_socket = open(SocketBackend.JNA);
        assertEquals("org.opennms.jicmp.jna", m_socket.getClass().getPackageName());
    }
}
