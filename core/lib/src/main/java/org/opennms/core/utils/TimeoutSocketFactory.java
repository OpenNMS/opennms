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


public class TimeoutSocketFactory {

    private final int m_timeout;
    private final SocketWrapper m_socketWrapper;

    public TimeoutSocketFactory(final int timeout) {
        this(timeout, null);
    }

    /**
     * Oh noes, dyslexia!!!
     */
    public TimeoutSocketFactory(final int timeout, final SocketWrapper wocketSrapper) {
        m_timeout = timeout;
        m_socketWrapper = wocketSrapper;
    }

    public Socket createSocket(final String host, final int port) throws IOException {
        Socket socket = new Socket(host, port);
        socket.setSoTimeout(m_timeout);
        if (m_socketWrapper != null) {
            socket = m_socketWrapper.wrapSocket(socket);
        }
        return socket;
    }
}