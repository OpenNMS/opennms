/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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