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
package org.opennms.jicmp.standalone;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.jicmp.jna.NativeDatagramSocket;

/**
 * JnaPinger
 * 
 * @deprecated This class seems like it's mostly a duplicate of 
 * {@link org.opennms.netmgt.icmp.jna.AbstractPinger<T>}.
 *
 * @author brozow
 */
public abstract class AbstractPinger<T extends InetAddress> implements Runnable {

    public static final double NANOS_PER_MILLI = 1000000.0;

    private NativeDatagramSocket m_pingSocket;
    private Thread m_thread;
    protected final AtomicReference<Throwable> m_throwable = new AtomicReference<Throwable>(null);
    protected final Metric m_metric = new Metric();
    private volatile boolean m_stopped = false;
    private final List<PingReplyListener> m_listeners = new ArrayList<>();

    protected AbstractPinger(NativeDatagramSocket pingSocket) {
        m_pingSocket = pingSocket;
    }

    /**
     * @return the pingSocket
     */
    protected NativeDatagramSocket getPingSocket() {
        return m_pingSocket;
    }

    public int getCount() {
        return m_metric.getCount();
    }

    public boolean isFinished() {
        return m_stopped;
    }

    public void start() {
        m_thread = new Thread(this, "PingThreadTest:PingListener");
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
    
    protected List<PingReplyListener> getListeners() {
        return m_listeners;
    }

    abstract public PingReplyMetric ping(T addr, int id, int sequenceNumber, int count, long interval) throws InterruptedException;

    public void addPingReplyListener(PingReplyListener listener) {
        m_listeners.add(listener);
    }

     PingReplyMetric ping(T addr4)  throws InterruptedException {
        Thread t = new Thread(this);
        t.start();
        return ping(addr4, 1234, 1, 10, 1000);
    }
}
