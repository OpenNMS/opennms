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

public abstract class SunNativeSocket extends NativeDatagramSocket {
    private static final Logger LOG = LoggerFactory.getLogger(SunNativeSocket.class);

    @Override
    protected IOException translateException(LastErrorException e) {
        final int errorCode = e.getErrorCode();
        IOException ret = new IOException(e.getMessage());
        switch (errorCode) {
        case 1:
            ret = new IOException("Operation not permitted (not super-user)");
            break;
        case 9:
            ret = new IOException("Bad file number");
            break;
        case 11:
            ret = new IOException("Resource temporarily unavailable");
            break;
        case 12:
            ret = new IOException("Not enough core");
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
            ret = new IOException("Inappropriate ioctl for device");
            break;
        case 95:
            ret = new SocketException("Socket operation on non-socket");
            break;
        case 96:
            ret = new SocketException("Destination address required");
            break;
        case 97:
            ret = new IOException("Message too long");
            break;
        case 98:
            ret = new SocketException("Protocol wrong type for socket");
            break;
        case 99:
            ret = new SocketException("Protocol not available");
            break;
        case 120:
            ret = new SocketException("Protocol not supported");
            break;
        case 121:
            ret = new SocketException("Socket type not supported");
            break;
        case 122:
            ret = new SocketException("Operation not supported on socket");
            break;
        case 123:
            ret = new SocketException("Protocol family not supported");
            break;
        case 124:
            ret = new SocketException("Address family not supported by protocol family");
            break;
        case 125:
            ret = new BindException("Address already in use");
            break;
        case 126:
            ret = new BindException("Cannot assign requested address");
            break;
        case 127:
            ret = new SocketException("Network is down");
            break;
        case 128:
            ret = new SocketException("Network is unreachable");
            break;
        case 129:
            ret = new SocketException("Network dropped connection because of reset");
            break;
        case 130:
            ret = new SocketException("Software caused connection abort");
            break;
        case 131:
            ret = new SocketException("Connection reset by peer");
            break;
        case 132:
            ret = new SocketException("No buffer space available");
            break;
        case 133:
            ret = new SocketException("Socket is already connected");
            break;
        case 134:
            ret = new SocketException("Socket is not connected");
            break;
        case 143:
            ret = new SocketException("Can't send after socket shutdown");
            break;
        case 144:
            ret = new SocketException("Too many references: can't splice");
            break;
        case 145:
            ret = new ConnectException("Connection timed out");
            break;
        case 146:
            ret = new ConnectException("Connection refused");
            break;
        case 147:
            ret = new IOException("Host is down");
            break;
        case 148:
            ret = new NoRouteToHostException(e.getMessage());
            break;
        case 149:
            ret = new IOException("Operation already in progress");
            break;
        case 150:
            ret = new IOException("Operation now in progress");
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
