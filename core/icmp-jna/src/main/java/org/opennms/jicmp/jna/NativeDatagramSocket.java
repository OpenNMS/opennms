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
package org.opennms.jicmp.jna;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * NativeDatagramSocket
 *
 * @author brozow
 */
public abstract class NativeDatagramSocket implements AutoCloseable {
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

    public static final int IPPROTO_IP = 0;
    public static final int IPPROTO_IPV6 = 41;
    public static final int IPPROTO_ICMP = 1;
    public static final int IPPROTO_UDP = 17;
    public static final int IPPROTO_ICMPV6 = 58;

    public static final int IP_MTU_DISCOVER = 10;
    public static final int IPV6_DONTFRAG = 62;

    // platform-specific  :/
    // public static final int IPV6_TCLASS = 36;

    public NativeDatagramSocket() {
        if (AF_INET6 == -1) {
            throw new UnsupportedPlatformException(System.getProperty("os.name"));
        }
    }
    
    public static NativeDatagramSocket create(final int family, final int protocol, final int listenPort) throws Exception {
        final String implClassName = NativeDatagramSocket.getImplementationClassName(family);
        LOG.debug("{}({}, {}, {})", implClassName, family, protocol, listenPort);
        final Class<? extends NativeDatagramSocket> implementationClass = Class.forName(implClassName).asSubclass(NativeDatagramSocket.class);
        final Constructor<? extends NativeDatagramSocket> constructor = implementationClass.getDeclaredConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
        try {
            return constructor.newInstance(family, SOCK_DGRAM, protocol, listenPort);
        } catch (final Exception e) {
            LOG.debug("Failed to create {} SOCK_DGRAM socket ({}).  Trying with SOCK_RAW.", implementationClass, e.getMessage());
            LOG.trace("Failed to create {} SOCK_DGRAM socket.  Trying with SOCK_RAW.", implementationClass, e);
            return constructor.newInstance(family, SOCK_RAW, protocol, listenPort);
        }
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

    public native String strerror(int errnum);
    public native int setsockopt(int socket, int level, int option_name, Pointer value, int option_len);

    public void allowFragmentation(final int level, final int option_name, final boolean frag) throws IOException {
        final int socket = getSock();
        if (socket < 0) {
            throw new IOException("Invalid socket!");
        }
        final IntByReference dontfragment = new IntByReference(frag == true? 0 : 1);
        try {
            setsockopt(socket, level, option_name, dontfragment.getPointer(), Native.POINTER_SIZE);
        } catch (final LastErrorException e) {
            throw new IOException("setsockopt: " + strerror(e.getErrorCode()));
        }
    }

    public abstract int getSock();
    public abstract void allowFragmentation(boolean frag) throws IOException;
    public abstract void setTrafficClass(int tc) throws IOException;
    public abstract int receive(NativeDatagramPacket p) throws UnknownHostException;
    public abstract int send(NativeDatagramPacket p);
    public abstract void close();
}
