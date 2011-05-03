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
package org.opennms.jicmp.standalone;

import static org.junit.Assume.assumeTrue;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.jna.Platform;


/**
 * PingThreadTest
 *
 * @author brozow
 */

public class PingThreadTest {
    
    @Before
    public void setUp() throws Exception {
        assumeTrue(Platform.isMac());
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testMultiThreadSocketUse() throws Exception {
        int pingCount = 10;
        V4Pinger listener = new V4Pinger();
        try {
        listener.start();
        
        listener.ping((Inet4Address)InetAddress.getByName("127.0.0.1"), 1000, 0, pingCount, 1000);
        
        } finally {
            listener.stop();
            listener.closeSocket();
            
        }
    }

    @Test
    public void testManyThreadSocketUse() throws Exception {
        V4Pinger listener = new V4Pinger();
        try {
        listener.start();
        
        Thread t1 = pingThead(listener, 1000, 5);
        Thread t2 = pingThead(listener, 2000, 5);
        Thread t3 = pingThead(listener, 3000, 5);
        Thread t4 = pingThead(listener, 4000, 5);
        Thread t5 = pingThead(listener, 5000, 5);
        
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        
        } finally {
            listener.stop();
            listener.closeSocket();
            
        }
    }

    private Thread pingThead(final V4Pinger listener, final int id, final int count) {
        return new Thread() {
            public void run() {
                try {
                    Thread.sleep(id/10);
                    listener.ping((Inet4Address)InetAddress.getByName("127.0.0.1"), id, 0, count, 1000);
                } catch(Throwable e) {
                    e.printStackTrace();
                }
            }
        };
    }


}
