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

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class Server implements Runnable {

    private AtomicReference<Throwable> m_exception = new AtomicReference<>();
    private AtomicBoolean m_stopped = new AtomicBoolean(false);
    private CountDownLatch m_latch = new CountDownLatch(1);
    private Thread m_thread;
    private int m_port;
    private DatagramSocket m_socket = null;

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

    public int getPort() {
        return m_socket == null ? 0 : m_socket.getLocalPort();
    }

    public InetAddress getInetAddress() {
        return m_socket.getLocalAddress();
    }

    @Override
    public void run() {
        try {
            m_socket = new DatagramSocket(m_port, InetAddress.getLocalHost());
            m_socket.setSoTimeout(500);
            m_latch.countDown();
            if (m_port == 0) {
                m_port = m_socket.getLocalPort();
            }

            while (!m_stopped.get()) {
                DatagramPacket p = new DatagramPacket(new byte[128], 128);
                m_socket.receive(p);
                String cmd = new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8);
                System.err.print(String.format("SERVER: %s\n", cmd));
                m_socket.send(p);
                if (cmd.startsWith("quit")) {
                    m_stopped.set(true);
                }
            }
            
        } catch (Exception e) {
            m_exception.set(e);
        } finally {
            if (m_socket != null) {
                m_socket.close();
            }
        }
    }

    
}
