/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.jicmp.jna;

import java.io.IOException;
import java.net.BindException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.rmi.ConnectException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.LastErrorException;

public abstract class UnixNativeSocket extends NativeDatagramSocket {
    private static final Logger LOG = LoggerFactory.getLogger(UnixNativeSocket.class);

    protected IOException translateException(final LastErrorException e) {
        final int errorCode = e.getErrorCode();
        IOException ret = new IOException(e.getMessage());
        switch (errorCode) {
        case 1:
            ret = new IOException("Operation not permitted");
            break;
        case 9:
            ret = new IOException("Bad file number");
            break;
        case 12:
            ret = new IOException("Out of memory");
            break;
        case 13:
            ret = new IOException("Permission denied");
            break;
        case 14:
            ret = new IOException("Bad address");
            break;
        case 23:
            ret = new IOException("File table overflow");
            break;
        case 24:
            ret = new IOException("Too many open files");
            break;
        case 25:
            ret = new IOException("Not a typewriter");
            break;
        case 35:
            ret = new IOException("Resource deadlock would occur");
            break;
        case 88:
            ret = new SocketException("Socket operation on non-socket");
            break;
        case 89:
            ret = new SocketException("Destination address required");
            break;
        case 90:
            ret = new IOException("Message too long");
            break;
        case 91:
            ret = new SocketException("Protocol wrong type for socket");
            break;
        case 92:
            ret = new SocketException("Protocol not available");
            break;
        case 93:
            ret = new SocketException("Protocol not supported");
            break;
        case 94:
            ret = new SocketException("Socket type not supported");
            break;
        case 95:
            ret = new SocketException("Operation not supported on transport endpoint");
            break;
        case 96:
            ret = new SocketException("Protocol family not supported");
            break;
        case 97:
            ret = new SocketException("Address family not supported by protocol");
            break;
        case 98:
            ret = new BindException("Address already in use");
            break;
        case 99:
            ret = new BindException("Cannot assign requested address");
            break;
        case 100:
            ret = new SocketException("Network is down");
            break;
        case 101:
            ret = new SocketException("Network is unreachable");
            break;
        case 102:
            ret = new SocketException("Network dropped connection because of reset");
            break;
        case 103:
            ret = new SocketException("Software caused connection abort");
            break;
        case 104:
            ret = new SocketException("Connection reset by peer");
            break;
        case 105:
            ret = new SocketException("No buffer space available");
            break;
        case 106:
            ret = new SocketException("Transport endpoint is already connected");
            break;
        case 107:
            ret = new SocketException("Transport endpoint is not connected");
            break;
        case 108:
            ret = new SocketException("Cannot send after transport endpoint shutdown");
            break;
        case 109:
            ret = new SocketException("Too many references: cannot splice");
            break;
        case 110:
            ret = new ConnectException("Connection timed out");
            break;
        case 111:
            ret = new ConnectException("Connection refused");
            break;
        case 112:
            ret = new IOException("Host is down");
            break;
        case 113:
            ret = new NoRouteToHostException(e.getMessage());
            break;
        case 114:
            ret = new SocketException("Operation already in progress");
            break;
        case 115:
            ret = new SocketException("Operation now in progress");
            break;
        default:
            LOG.warn("Unhandled errno {}={}", errorCode, this.strerror(errorCode), e);
            break;
        }
        if (ret != null) {
            ret.initCause(e);
        }
        LOG.debug("translate: returning {}", ret, ret);
        return ret;
    }
}
