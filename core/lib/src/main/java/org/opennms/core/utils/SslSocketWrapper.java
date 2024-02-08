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
package org.opennms.core.utils;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class SslSocketWrapper implements SocketWrapper {
    private final String[] m_cipherSuites;
    private final String m_protocol;

    public SslSocketWrapper() {
        this(null, null);
    }

    public SslSocketWrapper(String[] cipherSuites) {
        this(null, cipherSuites);
    }

    public SslSocketWrapper(String protocol, String[] cipherSuites) {
        m_protocol = protocol == null ? "SSL" : protocol;
        m_cipherSuites = cipherSuites == null ? null : Arrays.copyOf(cipherSuites, cipherSuites.length);
    }

    @Override
    public Socket wrapSocket(Socket socket) throws IOException {
        return SocketUtils.wrapSocketInSslContext(socket, m_protocol, m_cipherSuites);
    }
}