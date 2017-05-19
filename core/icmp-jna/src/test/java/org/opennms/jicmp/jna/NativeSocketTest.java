/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

/**
 * NativeSocketTest
 *
 * @author brozow
 */
@Ignore
public class NativeSocketTest {

    private static final ExecutorService m_executor = Executors.newCachedThreadPool();

    Server m_server;
    int m_port = 0;

    @Rule
    public TestName m_testName = new TestName();

    @Before
    public void setUp() throws Exception {
        System.err.println("------------------- begin " + m_testName.getMethodName() + " ---------------------");
        m_server = new Server(m_port);
        m_server.start();
        m_server.waitForStart();
        m_port = m_server.getPort();
    }

    @After
    public void tearDown() throws InterruptedException {
        m_server.stop();
        m_port = 0;
        System.err.println("------------------- end " + m_testName.getMethodName() + " -----------------------");
    }

    public void printf(final String fmt, final Object... args) {
        System.err.print(String.format(fmt, args));
    }

    @Test
    public void testServer() throws Exception  {
        String[] cmds = new String[] { "echo", "echo2", "quit" };
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();

            for(final String cmd : cmds) {
                final DatagramSocket sock = socket;
                final FutureTask<DatagramPacket> task = new FutureTask<DatagramPacket>(new Callable<DatagramPacket>() {
                    @Override public DatagramPacket call() throws Exception {
                        printf("Sending cmd: %s\n", cmd);

                        final byte[] data = cmd.getBytes(StandardCharsets.UTF_8);
                        final DatagramPacket p = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), sock.getLocalPort());
                        sock.send(p);

                        printf("Receiving...\n");
                        final DatagramPacket r = new DatagramPacket(new byte[128], 128);
                        sock.receive(r);
                        printf("Received\n");

                        return r;
                    }
                });

                m_executor.execute(task);
                final DatagramPacket r = task.get(10, TimeUnit.SECONDS);
                assertNotNull(r);

                final String response = new String(r.getData(), r.getOffset(), r.getLength(), StandardCharsets.UTF_8);
                printf("Received Response: %s from %s:%d\n", response, r.getAddress().getHostAddress(), r.getPort());
                assertEquals(cmd, response);
            }

        } finally {
            if (socket != null) socket.close();
        }
    }

    @Test
    public void testNativeV4() throws Exception {
        testNative(NativeDatagramSocket.PF_INET, InetAddress.getByName("127.0.0.1"));
    }

    @Test
    public void testNativeV6() throws Exception {
        testNative(NativeDatagramSocket.PF_INET6, InetAddress.getByName("::1"));
    }

    private void testNative(final int family, final InetAddress address) throws Exception {
        final String[] cmds = new String[] { "nativeEcho", "nativeEcho2", "quitNative" };
        NativeDatagramSocket socket = null;

        try {
            socket = NativeDatagramSocket.create(family, NativeDatagramSocket.IPPROTO_UDP, 1234);

            for(final String cmd : cmds) {
                final NativeDatagramSocket sock = socket;
                final FutureTask<NativeDatagramPacket> task = new FutureTask<NativeDatagramPacket>(new Callable<NativeDatagramPacket>() {
                    @Override public NativeDatagramPacket call() throws Exception {
                        printf("Sending cmd: %s\n", cmd);
                        final ByteBuffer buf = StandardCharsets.UTF_8.encode(cmd);
                        final NativeDatagramPacket p = new NativeDatagramPacket(buf, address, m_port);
                        sock.send(p);

                        printf("Receiving...\n");
                        final NativeDatagramPacket r = new NativeDatagramPacket(128);
                        sock.receive(r);
                        printf("Received.\n");

                        return r;
                    }

                });

                m_executor.execute(task);
                final NativeDatagramPacket r = task.get(10, TimeUnit.SECONDS);
                assertNotNull(r);

                final String response = StandardCharsets.UTF_8.decode(r.getContent()).toString();
                printf("Received Response: %s from %s:%d\n", response, r.getAddress().getHostAddress(), r.getPort());

                assertEquals(cmd, response);
            }

        } finally {
            if (socket != null) socket.close();
        }
    }

    @Test(timeout=10000)
    @Ignore("This is ignored since I haven't found a way to interrupt a socket blocked on recvfrom in linux")
    public void testCloseInReceive() throws Exception {
        try(final NativeDatagramSocket socket = NativeDatagramSocket.create(NativeDatagramSocket.PF_INET, NativeDatagramSocket.IPPROTO_UDP, 1234)) {
            final FutureTask<NativeDatagramPacket> task = new FutureTask<NativeDatagramPacket>(new Callable<NativeDatagramPacket>() {
                @Override public NativeDatagramPacket call() throws Exception {
                    final ByteBuffer buf = StandardCharsets.UTF_8.encode("msg1");
                    final NativeDatagramPacket p = new NativeDatagramPacket(buf, InetAddress.getLocalHost(), m_port);
                    socket.send(p);

                    final NativeDatagramPacket r = new NativeDatagramPacket(128);
                    printf("Receiving...\n");
                    socket.receive(r);
                    printf("Received\n");
                    return r;
                }
            });

            m_executor.execute(task);
            final NativeDatagramPacket r = task.get(10, TimeUnit.SECONDS);
            assertNotNull(r);

            final String response = StandardCharsets.UTF_8.decode(r.getContent()).toString();
            printf("Received Response: %s from %s:%d\n", response, r.getAddress().getHostAddress(), r.getPort());
        }
    }

}
