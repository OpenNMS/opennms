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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class Server implements Runnable {

    private AtomicReference<Exception > m_exception = new AtomicReference<Exception>();
    private AtomicBoolean m_stopped = new AtomicBoolean(false);
    private CountDownLatch m_latch = new CountDownLatch(1);
    private Thread m_thread;
    private int m_port;
    
    Server(int port) {
        m_port = port;
    }
    
    public void start() {
        m_thread = new Thread(this);
        m_thread.start();
    }
    
    public void waitForStart() throws InterruptedException {
        m_latch.await();
    }
    
    public boolean isStopped() {
        return m_stopped.get();
    }
    
    public void stop() throws InterruptedException {
        m_stopped.set(true);
        m_thread.join();
    }

    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(m_port);
            socket.setSoTimeout(500);
            m_latch.countDown();
            
            while (!m_stopped.get()) {
                DatagramPacket p = new DatagramPacket(new byte[128], 128);
                socket.receive(p);
                String cmd = new String(p.getData(), p.getOffset(), p.getLength(), "UTF-8");
                System.err.print(String.format("SERVER: %s\n", cmd));
                socket.send(p);
                if (cmd.startsWith("quit")) {
                    m_stopped.set(true);
                }
            }
            
        } catch (Exception e) {
            m_exception.set(e);
        } finally {
            if (socket != null) socket.close();
        }
    }
    
    
}