/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.icmp.jna;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.core.logging.Logging;
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
    private final AtomicReference<Throwable> m_throwable = new AtomicReference<Throwable>(null);
    private volatile boolean m_stopped = false;
    private final List<PingReplyListener> m_listeners = new ArrayList<PingReplyListener>();
    
    protected AbstractPinger(int pingerId, NativeDatagramSocket pingSocket) {
        m_pingerId = pingerId;
        m_pingSocket = pingSocket;
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
