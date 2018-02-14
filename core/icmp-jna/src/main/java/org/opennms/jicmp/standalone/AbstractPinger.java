/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
