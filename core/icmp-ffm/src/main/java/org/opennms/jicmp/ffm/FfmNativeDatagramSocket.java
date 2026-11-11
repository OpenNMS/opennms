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
package org.opennms.jicmp.ffm;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;
import org.opennms.jicmp.jna.NativeOs;
import org.opennms.jicmp.jna.SocketUtils;

/**
 * {@link NativeDatagramSocket} implemented with the Java Foreign Function
 * &amp; Memory API (JEP 454), calling the C library's socket functions
 * directly instead of going through JNA.
 *
 * <p>Supports IPv4 and IPv6 on Linux and on BSD-style systems (macOS,
 * FreeBSD, OpenBSD). On other platforms the constructor throws
 * {@link UnsupportedOperationException} so that
 * {@link NativeDatagramSocket#create} falls back to JNA.
 *
 * <p>Bytes are sent and received exactly as the JNA implementations do, so
 * the pinger logic on top is unchanged. Assumes an LP64 platform.
 */
public final class FfmNativeDatagramSocket extends NativeDatagramSocket {

    private static final int EINTR = 4;

    // Socket option numbers differ per OS; values taken from each system's
    // <netinet/in.h> and <netinet6/in6.h> (Linux: <linux/in.h>, <linux/in6.h>).
    private static final int LINUX_IP_TOS = 1;
    private static final int BSD_IP_TOS = 3;             // macOS, FreeBSD, OpenBSD
    private static final int LINUX_IPV6_TCLASS = 67;
    private static final int MACOS_IPV6_TCLASS = 36;
    private static final int BSD_IPV6_TCLASS = 61;       // FreeBSD, OpenBSD
    private static final int LINUX_IP_MTU_DISCOVER = 10;
    private static final int MACOS_IP_DONTFRAG = 28;
    private static final int FREEBSD_IP_DONTFRAG = 67;   // OpenBSD has no IPv4 IP_DONTFRAG

    private static final int SOCKADDR_IN_SIZE = 16;
    private static final int SOCKADDR_IN6_SIZE = 28;
    private static final int RECEIVE_BUFFER_SIZE = 65535;

    private final int m_family;
    private final boolean m_bsd;
    private final MemorySegment m_receiveBuffer;
    private final MemorySegment m_receiveAddress;
    private final MemorySegment m_receiveAddressLength;
    private final MemorySegment m_receiveErrno;
    private volatile int m_sock;

    public FfmNativeDatagramSocket(final int family, final int type, final int protocol, final int listenPort) throws Exception {
        if (!(NativeOs.isLinux() || NativeOs.isBsdFamily())) {
            throw new UnsupportedOperationException("FFM sockets are not supported on " + NativeOs.name());
        }
        if (family != AF_INET && family != AF_INET6) {
            throw new IllegalArgumentException("Unsupported protocol family: " + family);
        }
        final Libc libc = Libc.get();
        m_family = family;
        m_bsd = NativeOs.isBsdFamily();

        // receive() is only called by the pinger's reader thread; reuse its buffers
        final Arena arena = Arena.ofAuto();
        m_receiveBuffer = arena.allocate(RECEIVE_BUFFER_SIZE);
        m_receiveAddress = arena.allocate(sockaddrSize());
        m_receiveAddressLength = arena.allocate(JAVA_INT);
        m_receiveErrno = arena.allocate(Libc.CAPTURE_LAYOUT);

        try (Arena call = Arena.ofConfined()) {
            final MemorySegment errno = call.allocate(Libc.CAPTURE_LAYOUT);
            final int sock = (int) libc.socket.invokeExact(errno, family, type, protocol);
            if (sock < 0) {
                throw libc.ioError("socket", errno);
            }
            final MemorySegment addr = encodeAddress(call, null, listenPort);
            final int rc = (int) libc.bind.invokeExact(errno, sock, addr, (int) addr.byteSize());
            if (rc < 0) {
                final IOException e = libc.ioError("bind", errno);
                closeQuietly(libc, sock);
                throw e;
            }
            m_sock = sock;
        } catch (final Exception e) {
            throw e;
        } catch (final Throwable t) {
            throw new IOException("Unable to create socket: " + t, t);
        }
    }

    @Override
    public int getSock() {
        return m_sock;
    }

    @Override
    public void setTrafficClass(final int tc) throws IOException {
        if (m_family == AF_INET) {
            setIntOption(IPPROTO_IP, m_bsd ? BSD_IP_TOS : LINUX_IP_TOS, tc);
        } else {
            setIntOption(IPPROTO_IPV6, ipv6TclassOption(), tc);
        }
    }

    @Override
    public void allowFragmentation(final boolean frag) throws IOException {
        // Same values as the JNA implementations: 0 allows fragmentation, 1 does not.
        final int value = frag ? 0 : 1;
        if (m_family == AF_INET6) {
            setIntOption(IPPROTO_IPV6, IPV6_DONTFRAG, value);
        } else if (NativeOs.isLinux()) {
            setIntOption(IPPROTO_IP, LINUX_IP_MTU_DISCOVER, value);
        } else if (NativeOs.isMac()) {
            setIntOption(IPPROTO_IP, MACOS_IP_DONTFRAG, value);
        } else if (NativeOs.isFreeBSD()) {
            setIntOption(IPPROTO_IP, FREEBSD_IP_DONTFRAG, value);
        }
        // OpenBSD has no per-socket DF option for IPv4, same as the JNA BSDV4NativeSocket.
    }

    private static int ipv6TclassOption() {
        if (NativeOs.isLinux()) {
            return LINUX_IPV6_TCLASS;
        }
        if (NativeOs.isMac()) {
            return MACOS_IPV6_TCLASS;
        }
        return BSD_IPV6_TCLASS;
    }

    @Override
    public int receive(final NativeDatagramPacket p) throws UnknownHostException {
        final int socket = getSock();
        SocketUtils.assertSocketValid(socket);
        final Libc libc = Libc.get();
        final ByteBuffer buf = p.getContent();
        final int capacity = Math.min(buf.capacity(), RECEIVE_BUFFER_SIZE);
        long n;
        try {
            do {
                m_receiveAddressLength.set(JAVA_INT, 0, (int) m_receiveAddress.byteSize());
                n = (long) libc.recvfrom.invokeExact(m_receiveErrno, socket, m_receiveBuffer, (long) capacity, 0,
                        m_receiveAddress, m_receiveAddressLength);
            } while (n < 0 && Libc.errno(m_receiveErrno) == EINTR);
        } catch (final Throwable t) {
            throw new NativeSocketException("recvfrom failed: " + t, -1, t);
        }
        if (n < 0) {
            throw libc.socketError("recvfrom", m_receiveErrno);
        }
        final int length = (int) n;
        final ByteBuffer target = buf.duplicate();
        target.clear();
        target.put(m_receiveBuffer.asSlice(0, length).asByteBuffer());
        p.setLength(length);
        p.setAddress(decodeAddress(m_receiveAddress));
        p.setPort(decodePort(m_receiveAddress));
        return length;
    }

    @Override
    public int send(final NativeDatagramPacket p) {
        final int socket = getSock();
        SocketUtils.assertSocketValid(socket);
        final Libc libc = Libc.get();
        final ByteBuffer buf = p.getContent();
        try (Arena call = Arena.ofConfined()) {
            final MemorySegment errno = call.allocate(Libc.CAPTURE_LAYOUT);
            final MemorySegment data = call.allocate(Math.max(1, buf.remaining()));
            data.asByteBuffer().put(buf.duplicate());
            final MemorySegment dest = encodeAddress(call, p.getAddress(), p.getPort());
            long n;
            do {
                n = (long) libc.sendto.invokeExact(errno, socket, data, (long) buf.remaining(), 0, dest, (int) dest.byteSize());
            } while (n < 0 && Libc.errno(errno) == EINTR);
            if (n < 0) {
                throw libc.socketError("sendto", errno);
            }
            return (int) n;
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Throwable t) {
            throw new NativeSocketException("sendto failed: " + t, -1, t);
        }
    }

    @Override
    public void close() {
        final int socket = m_sock;
        m_sock = -1;
        if (socket > 0) {
            closeQuietly(Libc.get(), socket);
        }
    }

    private static void closeQuietly(final Libc libc, final int socket) {
        try (Arena call = Arena.ofConfined()) {
            final int rc = (int) libc.close.invokeExact(call.allocate(Libc.CAPTURE_LAYOUT), socket);
        } catch (final Throwable t) {
            // nothing useful to do
        }
    }

    private void setIntOption(final int level, final int option, final int value) throws IOException {
        final int socket = getSock();
        if (socket < 0) {
            throw new IOException("Invalid socket!");
        }
        final Libc libc = Libc.get();
        try (Arena call = Arena.ofConfined()) {
            final MemorySegment errno = call.allocate(Libc.CAPTURE_LAYOUT);
            final MemorySegment val = call.allocateFrom(JAVA_INT, value);
            final int rc = (int) libc.setsockopt.invokeExact(errno, socket, level, option, val, (int) JAVA_INT.byteSize());
            if (rc < 0) {
                throw libc.ioError("setsockopt", errno);
            }
        } catch (final IOException e) {
            throw e;
        } catch (final Throwable t) {
            throw new IOException("setsockopt failed: " + t, t);
        }
    }

    private int sockaddrSize() {
        return m_family == AF_INET ? SOCKADDR_IN_SIZE : SOCKADDR_IN6_SIZE;
    }

    /**
     * Encodes a {@code sockaddr_in}/{@code sockaddr_in6}. Linux starts with a
     * 16-bit family; BSD starts with a length byte and an 8-bit family.
     * Port and address are in network byte order.
     */
    private MemorySegment encodeAddress(final Arena arena, final InetAddress address, final int port) {
        final int size = sockaddrSize();
        final MemorySegment sa = arena.allocate(size);
        if (m_bsd) {
            sa.set(JAVA_BYTE, 0, (byte) size);
            sa.set(JAVA_BYTE, 1, (byte) m_family);
        } else {
            sa.set(JAVA_SHORT, 0, (short) m_family);
        }
        sa.set(JAVA_BYTE, 2, (byte) (0xff & (port >> 8)));
        sa.set(JAVA_BYTE, 3, (byte) (0xff & port));
        if (address != null) {
            final byte[] addr = address.getAddress();
            final int expected = m_family == AF_INET ? 4 : 16;
            if (addr.length != expected) {
                throw new IllegalArgumentException("address length must be " + expected + " bytes but was " + addr.length + " bytes.");
            }
            MemorySegment.copy(addr, 0, sa, JAVA_BYTE, m_family == AF_INET ? 4 : 8, addr.length);
        }
        return sa;
    }

    private InetAddress decodeAddress(final MemorySegment sa) throws UnknownHostException {
        final byte[] addr = new byte[m_family == AF_INET ? 4 : 16];
        MemorySegment.copy(sa, JAVA_BYTE, m_family == AF_INET ? 4 : 8, addr, 0, addr.length);
        return InetAddress.getByAddress(addr);
    }

    private static int decodePort(final MemorySegment sa) {
        return ((sa.get(JAVA_BYTE, 2) & 0xff) << 8) | (sa.get(JAVA_BYTE, 3) & 0xff);
    }

    /** Lazily bound C library functions, shared by all sockets. */
    static final class Libc {
        static final MemoryLayout CAPTURE_LAYOUT = Linker.Option.captureStateLayout();
        private static final VarHandle ERRNO = CAPTURE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("errno"));
        private static volatile Libc instance;

        final MethodHandle socket;
        final MethodHandle bind;
        final MethodHandle setsockopt;
        final MethodHandle sendto;
        final MethodHandle recvfrom;
        final MethodHandle close;
        private final MethodHandle strerror;

        static Libc get() {
            Libc libc = instance;
            if (libc == null) {
                synchronized (Libc.class) {
                    libc = instance;
                    if (libc == null) {
                        libc = new Libc();
                        instance = libc;
                    }
                }
            }
            return libc;
        }

        private Libc() {
            final Linker linker = Linker.nativeLinker();
            final SymbolLookup lookup = linker.defaultLookup();
            final Linker.Option errno = Linker.Option.captureCallState("errno");
            // int socket(int domain, int type, int protocol)
            socket = bind(linker, lookup, "socket", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT), errno);
            // int bind(int fd, const struct sockaddr *addr, socklen_t len)
            bind = bind(linker, lookup, "bind", FunctionDescriptor.of(JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT), errno);
            // int setsockopt(int fd, int level, int name, const void *value, socklen_t len)
            setsockopt = bind(linker, lookup, "setsockopt", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS, JAVA_INT), errno);
            // ssize_t sendto(int fd, const void *buf, size_t len, int flags, const struct sockaddr *dest, socklen_t len)
            sendto = bind(linker, lookup, "sendto", FunctionDescriptor.of(JAVA_LONG, JAVA_INT, ADDRESS, JAVA_LONG, JAVA_INT, ADDRESS, JAVA_INT), errno);
            // ssize_t recvfrom(int fd, void *buf, size_t len, int flags, struct sockaddr *src, socklen_t *len)
            recvfrom = bind(linker, lookup, "recvfrom", FunctionDescriptor.of(JAVA_LONG, JAVA_INT, ADDRESS, JAVA_LONG, JAVA_INT, ADDRESS, ADDRESS), errno);
            // int close(int fd)
            close = bind(linker, lookup, "close", FunctionDescriptor.of(JAVA_INT, JAVA_INT), errno);
            // char *strerror(int errnum)
            strerror = bind(linker, lookup, "strerror", FunctionDescriptor.of(ADDRESS, JAVA_INT));
        }

        private static MethodHandle bind(final Linker linker, final SymbolLookup lookup, final String name,
                                         final FunctionDescriptor fd, final Linker.Option... options) {
            final MemorySegment symbol = lookup.find(name)
                    .orElseThrow(() -> new UnsatisfiedLinkError("C library symbol not found: " + name));
            return linker.downcallHandle(symbol, fd, options);
        }

        static int errno(final MemorySegment capture) {
            return (int) ERRNO.get(capture, 0L);
        }

        String strerror(final int errnum) {
            try {
                final MemorySegment msg = (MemorySegment) strerror.invokeExact(errnum);
                return msg.equals(MemorySegment.NULL) ? "errno " + errnum : msg.reinterpret(Long.MAX_VALUE).getString(0);
            } catch (final Throwable t) {
                return "errno " + errnum;
            }
        }

        IOException ioError(final String function, final MemorySegment capture) {
            final int errnum = errno(capture);
            return new IOException(function + ": " + strerror(errnum) + " (errno " + errnum + ")");
        }

        NativeSocketException socketError(final String function, final MemorySegment capture) {
            final int errnum = errno(capture);
            return new NativeSocketException(function + ": " + strerror(errnum), errnum, null);
        }
    }
}
