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
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.LastErrorException;

public abstract class BSDNativeSocket extends NativeDatagramSocket {
    private static final Logger LOG = LoggerFactory.getLogger(BSDNativeSocket.class);

    @Override
    protected IOException translateException(LastErrorException e) {
        final int errorCode = e.getErrorCode();
        IOException ret = new IOException(e.getMessage());
        switch (errorCode) {
        case 1:
            ret = new IOException("Operation not permitted");
            break;
        case 9:
            ret = new IOException("Bad file descriptor");
            break;
        case 12:
            ret = new IOException("Cannot allocate memory");
            break;
        case 13:
            ret = new IOException("Permission denied");
            break;
        case 14:
            ret = new IOException("Bad address");
            break;
        case 23:
            ret = new IOException("Too many open files in system");
            break;
        case 24:
            ret = new IOException("Too many open files");
            break;
        case 25:
            ret = new IOException("Inappropriate ioctl for device");
            break;
        case 35:
            ret = new IOException("Resource temporarily unavailable");
            break;
        case 36:
            ret = new IOException("Operation now in progress");
            break;
        case 37:
            ret = new IOException("Operation already in progress");
            break;
        case 38:
            ret = new SocketException("Socket operation on non-socket");
            break;
        case 39:
            ret = new SocketException("Destination address required");
            break;
        case 40:
            ret = new IOException("Message too long");
            break;
        case 41:
            ret = new SocketException("Protocol wrong type for socket");
            break;
        case 42:
            ret = new SocketException("Protocol not available");
            break;
        case 43:
            ret = new SocketException("Protocol not supported");
            break;
        case 44:
            ret = new SocketException("Socket type not supported");
            break;
        case 45:
            ret = new SocketException("Operation not supported");
            break;
        case 46:
            ret = new SocketException("Protocol family not supported");
            break;
        case 47:
            ret = new SocketException("Address family not supported by protocol family");
            break;
        case 48:
            ret = new BindException("Address already in use");
            break;
        case 49:
            ret = new BindException("Cannot assign requested address");
            break;
        case 50:
            ret = new SocketException("Network is down");
            break;
        case 51:
            ret = new SocketException("Network is unreachable");
            break;
        case 52:
            ret = new SocketException("Network dropped connection on reset");
            break;
        case 53:
            ret = new SocketException("Software caused connection abort");
            break;
        case 54:
            ret = new SocketException("Connection reset by peer");
            break;
        case 55:
            ret = new SocketException("No buffer space available");
            break;
        case 56:
            ret = new SocketException("Socket is already connected");
            break;
        case 57:
            ret = new SocketException("Socket is not connected");
            break;
        case 58:
            ret = new SocketException("Can't send after socket shutdown");
            break;
        case 59:
            ret = new SocketException("Too many references: can't splice");
            break;
        case 60:
            ret = new ConnectException("Operation timed out");
            break;
        case 61:
            ret = new ConnectException("Connection refused");
            break;
        case 64:
            ret = new IOException("Host is down");
            break;
        case 65:
            ret = new NoRouteToHostException(e.getMessage());
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
