/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.jicmp.jna;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.net.InetAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.junit.Test;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
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

    
    @Test
    public void testWrap() throws Exception {
        
        String msg = "OpenNMS!";
        
        byte[] data = msg.getBytes("US-ASCII");

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
        
        Charset ascii = Charset.forName("US-ASCII");

        ByteBuffer buf = ascii.encode("OpenNMS!");
        
        String decoded = ascii.decode(buf).toString();
        
        assertThat(decoded, is(equalTo("OpenNMS!")));
        
        
        
    }
    
    @Test
    public void testPassing() throws Exception {
        Server server = new Server(7777);
        server.start();
        server.waitForStart();

        String msg = "OpenNMS!";

        
        int socket = -1;
        try {
            
            byte[] data = msg.getBytes("US-ASCII");
            String sent = msg.substring(4,7);
            ByteBuffer buf = ByteBuffer.wrap(data, 4, 3).slice();
            
            socket = socket(NativeDatagramSocket.PF_INET, NativeDatagramSocket.SOCK_DGRAM, NativeDatagramSocket.IPPROTO_UDP);

            sockaddr_in destAddr = new sockaddr_in(InetAddress.getLocalHost(), 7777);
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
            
            String results = new String(b, "US-ASCII");
            
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
