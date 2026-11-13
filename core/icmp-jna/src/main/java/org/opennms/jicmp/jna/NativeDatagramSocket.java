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
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NativeDatagramSocket
 *
 * <p>Sockets are created by {@link #create(int, int, int)}, which loads the
 * Java Foreign Function &amp; Memory implementation
 * ({@value #FFM_IMPLEMENTATION}). It supports Linux, macOS and FreeBSD.
 *
 * @author brozow
 */
public abstract class NativeDatagramSocket implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(NativeDatagramSocket.class);

    public static final int AF_INET = 2;
    public static final int PF_INET = AF_INET;
    public static final int AF_INET6 = NativeOs.isMac() ? 30
                                     : NativeOs.isLinux() ? 10
                                     : NativeOs.isFreeBSD() ? 28
                                     : -1;
    public static final int PF_INET6 = AF_INET6;

    public static final int SOCK_DGRAM = 2;
    public static final int SOCK_RAW = 3;

    public static final int IPPROTO_IP = 0;
    public static final int IPPROTO_IPV6 = 41;
    public static final int IPPROTO_ICMP = 1;
    public static final int IPPROTO_UDP = 17;
    public static final int IPPROTO_ICMPV6 = 58;

    public static final int IP_MTU_DISCOVER = 10;
    public static final int IPV6_DONTFRAG = 62;

    // platform-specific  :/
    // public static final int IPV6_TCLASS = 36;

    public static final String FFM_IMPLEMENTATION = "org.opennms.jicmp.ffm.FfmNativeDatagramSocket";

    public NativeDatagramSocket() {
        if (AF_INET6 == -1) {
            throw new UnsupportedPlatformException(System.getProperty("os.name"));
        }
    }

    public static NativeDatagramSocket create(final int family, final int protocol, final int listenPort) throws Exception {
        return createFfm(family, protocol, listenPort);
    }

    private static NativeDatagramSocket createFfm(final int family, final int protocol, final int listenPort) throws Exception {
        final Class<? extends NativeDatagramSocket> implementationClass;
        try {
            implementationClass = Class.forName(FFM_IMPLEMENTATION, true, NativeDatagramSocket.class.getClassLoader()).asSubclass(NativeDatagramSocket.class);
        } catch (final ClassNotFoundException | LinkageError e) {
            throw new UnsupportedOperationException("FFM socket implementation is not available: " + e, e);
        }
        return newInstance(implementationClass, family, protocol, listenPort);
    }

    /** Tries a {@code SOCK_DGRAM} (unprivileged) socket first, then {@code SOCK_RAW}. */
    private static NativeDatagramSocket newInstance(final Class<? extends NativeDatagramSocket> implementationClass, final int family, final int protocol, final int listenPort) throws Exception {
        LOG.debug("{}({}, {}, {})", implementationClass.getName(), family, protocol, listenPort);
        final Constructor<? extends NativeDatagramSocket> constructor = implementationClass.getDeclaredConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
        try {
            return constructor.newInstance(family, SOCK_DGRAM, protocol, listenPort);
        } catch (final Exception e) {
            LOG.debug("Failed to create {} SOCK_DGRAM socket ({}).  Trying with SOCK_RAW.", implementationClass, e.getMessage());
            LOG.trace("Failed to create {} SOCK_DGRAM socket.  Trying with SOCK_RAW.", implementationClass, e);
            try {
                return constructor.newInstance(family, SOCK_RAW, protocol, listenPort);
            } catch (final InvocationTargetException ite) {
                if (ite.getCause() instanceof Exception) {
                    throw (Exception) ite.getCause();
                }
                throw ite;
            }
        }
    }

    public abstract int getSock();
    public abstract void allowFragmentation(boolean frag) throws IOException;
    public abstract void setTrafficClass(int tc) throws IOException;
    public abstract int receive(NativeDatagramPacket p) throws UnknownHostException;
    public abstract int send(NativeDatagramPacket p);
    public abstract void close();
}
