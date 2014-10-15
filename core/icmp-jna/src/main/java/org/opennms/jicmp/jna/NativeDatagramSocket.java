/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.jicmp.jna;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Platform;

/**
 * NativeDatagramSocket
 *
 * @author brozow
 */
public abstract class NativeDatagramSocket {
    private static final Logger LOG = LoggerFactory.getLogger(NativeDatagramSocket.class);

    public static final int AF_INET = 2;
    public static final int PF_INET = AF_INET;
    public static final int AF_INET6 = Platform.isMac() ? 30 
                                     : Platform.isLinux() ? 10 
                                     : Platform.isWindows() ? 23 
                                     : Platform.isFreeBSD() ? 28 
                                     : Platform.isSolaris() ? 26 
                                     : -1;
    public static final int PF_INET6 = AF_INET6;

    public static final int SOCK_DGRAM = Platform.isSolaris() ? 1 
                                        : 2;
    public static final int SOCK_RAW = Platform.isSolaris() ? 4 
                                     : 3;

    public static final int IPPROTO_ICMP = 1;
    public static final int IPPROTO_UDP = 17;
    public static final int IPPROTO_ICMPV6 = 58;

    public NativeDatagramSocket() {
        if (AF_INET6 == -1) {
            throw new UnsupportedPlatformException(System.getProperty("os.name"));
        }
    }
    
    public static NativeDatagramSocket create(final int family, final int type, final int protocol) throws Exception {
        final String implClassName = NativeDatagramSocket.getImplementationClassName(family);
        LOG.debug("{}({}, {}, {})", implClassName, family, type, protocol);
        final Class<? extends NativeDatagramSocket> implementationClass = Class.forName(implClassName).asSubclass(NativeDatagramSocket.class);
        return implementationClass.getDeclaredConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(family, type, protocol);
    }

    private static String getClassPackage() {
        return NativeDatagramSocket.class.getPackage().getName();
    }
    
    private static String getClassPrefix() {
        return Platform.isWindows() ? "Win32" 
              : Platform.isSolaris() ? "Sun" 
              : (Platform.isMac() || Platform.isFreeBSD() || Platform.isOpenBSD()) ? "BSD" 
              : "Unix";
    }

    private static String getFamilyPrefix(int family) {
        if (AF_INET == family) {
            return "V4";
        } else if (AF_INET6 == family) {
            return "V6";
        } else {
            throw new IllegalArgumentException("Unsupported Protocol Family: "+ family);
        }
    }
    
    private static String getImplementationClassName(int family) {
        return NativeDatagramSocket.getClassPackage()+
            "."+
            NativeDatagramSocket.getClassPrefix()+
            NativeDatagramSocket.getFamilyPrefix(family)+
            "NativeSocket";
    }

    public abstract int receive(NativeDatagramPacket p) throws UnknownHostException;
    public abstract int send(NativeDatagramPacket p);
    public abstract int close();
    
}
