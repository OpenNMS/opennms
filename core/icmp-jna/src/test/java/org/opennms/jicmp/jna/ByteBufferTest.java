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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.InetAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.ptr.IntByReference;


/**
 * ByteBufferTest
 *
 * @author brozow
 */
public class ByteBufferTest {
    
    static {
        Native.register((String)null);
    }     

    public native int socket(int domain, int type, int protocol) throws LastErrorException;
    public native int sendto(int socket, Buffer buffer, int buflen, int flags, sockaddr_in dest_addr, int dest_addr_len) throws LastErrorException;
    public native int recvfrom(int socket, Buffer buffer, int buflen, int flags, sockaddr_in in_addr, IntByReference in_addr_len) throws LastErrorException;
    //public native int close(int socket) throws LastErrorException;

    
    public void printf(String fmt, Object... args) {
        System.err.print(String.format(fmt, args));
    }

    @Rule
    public TestName m_testName = new TestName();

    @Before
    public void setUp() throws Exception {
        System.err.println("------------------- begin " + m_testName.getMethodName() + " ---------------------");
    }

    @After
    public void tearDown() throws InterruptedException {
        System.err.println("------------------- end " + m_testName.getMethodName() + " -----------------------");
    }

    @Test
    public void testWrap() {
        
        String msg = "OpenNMS!";
        
        byte[] data = msg.getBytes(StandardCharsets.US_ASCII);

        ByteBuffer buf = ByteBuffer.wrap(data, 2, 4);
        
        assertThat(buf.arrayOffset(), is(equalTo(0)));
        assertThat(buf.position(), is(equalTo(2)));
        assertThat(buf.limit(), is(equalTo(6)));
        assertThat(buf.capacity(), is(equalTo(data.length)));
        
        assertThat(buf.get(0), is(equalTo((byte)'O')));
        
    }
    
    @Test
    public void testStringDecoding() {
        
        /*
         *  attempt to decode a string from a byte buffer without
         *  accessing the byte array that may or may NOT be behind it
         */
        
        ByteBuffer buf = StandardCharsets.US_ASCII.encode("OpenNMS!");
        
        String decoded = StandardCharsets.US_ASCII.decode(buf).toString();
        
        assertThat(decoded, is(equalTo("OpenNMS!")));
    }
    
    @Test(timeout=30000)
    public void testPassing() throws Exception {
        if (Platform.isMac() || Platform.isFreeBSD() || Platform.isOpenBSD()) {
            printf("sockaddr_in is incompatible with bsd_sockaddr_in\n");
            return;
        }
        Server server = new Server(0);
        server.start();
        server.waitForStart();
        int port = server.getPort();

        String msg = "OpenNMS!";

        
        int socket = -1;
        try {
            
            byte[] data = msg.getBytes(StandardCharsets.US_ASCII);
            String sent = msg.substring(4,7);
            ByteBuffer buf = ByteBuffer.wrap(data, 4, 3).slice();

            socket = socket(NativeDatagramSocket.PF_INET, NativeDatagramSocket.SOCK_DGRAM, NativeDatagramSocket.IPPROTO_UDP);

            sockaddr_in destAddr = new sockaddr_in(InetAddress.getLocalHost(), port);
            sendto(socket, buf, buf.remaining(), 0, destAddr, destAddr.size());


            sockaddr_in in_addr = new sockaddr_in();
            IntByReference szRef = new IntByReference(in_addr.size());
            
            ByteBuffer rBuf = ByteBuffer.allocate(128);
            int n = recvfrom(socket, rBuf, rBuf.remaining(), 0, in_addr, szRef);
            rBuf.limit(rBuf.position()+n);
            
            assertThat(szRef.getValue(), is(equalTo(in_addr.size())));
            assertThat(rBuf.isDirect(), is(false));
            assertThat(rBuf.position(), is(equalTo(0)));
            assertThat(rBuf.limit(), is(equalTo(n)));
            assertThat(rBuf.capacity(), is(equalTo(128)));
            
            byte[] b = new byte[rBuf.remaining()];
            rBuf.get(b);
            
            String results = new String(b, StandardCharsets.US_ASCII);
            
            printf("Received: %s\n", results);
            
            assertEquals(sent, results);
            
                
        } finally {
            // we leak this socket since close doesn't work on windows
            // it will go away when the test exits
            //if (socket != -1) close(socket);
            
            server.stop();

        }

        
    }

}
