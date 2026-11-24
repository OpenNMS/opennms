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
package org.opennms.netmgt.icmp.jna;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;
import org.opennms.netmgt.icmp.EchoPacket;

/**
 * JnaPinger
 *
 * @author brozow
 */
public abstract class AbstractPinger<T extends InetAddress> implements Runnable {

    public static final double NANOS_PER_MILLI = 1000000.0;

    private int m_pingerId;
    private NativeDatagramSocket m_pingSocket;
    private Thread m_thread;
    protected final AtomicReference<Throwable> m_throwable = new AtomicReference<Throwable>(null);
    private volatile boolean m_stopped = false;
    private final List<PingReplyListener> m_listeners = new ArrayList<>();

    protected AbstractPinger(int pingerId, NativeDatagramSocket pingSocket) {
        m_pingerId = pingerId;
        m_pingSocket = pingSocket;
    }

    /** Largest datagram the receive loop accepts. */
    protected static final int MAX_DATAGRAM_SIZE = 65535;

    /**
     * Returns an exact-size copy of a datagram received into a reused buffer.
     *
     * The receive loop reuses one {@value #MAX_DATAGRAM_SIZE}-byte buffer per pinger
     * instead of allocating a new one per reply, which dominated CPU and GC at high
     * ping rates. Parsed replies keep a view of the packet bytes, so each reply gets
     * its own copy.
     */
    protected static NativeDatagramPacket copyOf(final NativeDatagramPacket received) {
        final ByteBuffer content = received.getContent();
        final byte[] data = new byte[content.remaining()];
        content.get(data);
        return new NativeDatagramPacket(data, received.getAddress(), received.getPort());
    }

    /**
     * @return the pingSocket
     */
    protected NativeDatagramSocket getPingSocket() {
        return m_pingSocket;
    }
    
    protected int getPingerId() {
        return m_pingerId;
    }

    public boolean isFinished() {
        return m_stopped;
    }

    public void start() {
        m_thread = new Thread(this, "JNA-ICMP-"+getClass().getSimpleName()+"-"+m_pingerId+"-Socket-Reader");
        m_thread.setDaemon(true);
        m_thread.start();
    }

    public void stop() throws InterruptedException {
        m_stopped = true;
        if (m_thread != null) {
            m_thread.interrupt();
            //m_thread.join();
        }
        m_thread = null;
    }

    public void closeSocket() {
        if (getPingSocket() != null) {
            getPingSocket().close();
        }
    }

    abstract public void ping(T addr, int identifier, int sequenceNumber, long threadId, long count, long interval, int packetSize) throws InterruptedException;

    public void addPingReplyListener(PingReplyListener listener) {
        m_listeners.add(listener);
    }

    protected void notifyPingListeners(InetAddress address, EchoPacket echoReply) {
        for (PingReplyListener listener : m_listeners) {
            listener.onPingReply(address, echoReply);
        }
    }

    protected void setThrowable(Throwable e) {
        m_throwable.set(e);
    }
}
