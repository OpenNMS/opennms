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
package org.opennms.jicmp;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;

import org.opennms.jicmp.jna.NativeDatagramSocket;

/**
 * Pinger
 *
 * @author brozow
 */
public abstract class Pinger implements Runnable {

    private NativeDatagramSocket m_pingSocket;
    private Thread m_thread;
    protected AtomicReference<Throwable> m_throwable = new AtomicReference<Throwable>(null);
    protected Metric m_metric = new Metric();
    private volatile boolean m_stopped = false;
    
    protected Pinger(NativeDatagramSocket pingSocket) {
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

    protected void printf(String fmt, Object... args) {
        System.err.print(String.format(fmt, args));
    }
    
    abstract public void ping(InetAddress addr, int id, int count, int interval) throws InterruptedException;

}
