/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 17, 2006
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.daemon;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.ServiceDaemon;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public abstract class AbstractServiceDaemon implements ServiceDaemon, SpringServiceDaemon {
    public final void afterPropertiesSet() throws Exception {
        init();
    }

    /**
     * The current status of this fiber
     */
    private int m_status;

    private String m_name;
    
    private Object m_statusLock = new Object();

    abstract protected void onInit();

    protected void onPause() {}

    protected void onResume() {}

    protected void onStart() {}

    protected void onStop() {}

    final public String getName() { return m_name; }

    protected AbstractServiceDaemon(String name) {
        m_name = name;
        setStatus(START_PENDING);
    }

    protected void setStatus(int status) {
        synchronized (m_statusLock) {
            m_status = status;
            m_statusLock.notifyAll();
        }
    }
    
    protected void waitForStatus(int status, long timeout) throws InterruptedException {
        synchronized (m_statusLock) {
            
            long last = System.currentTimeMillis();
            long waitTime = timeout;
            while (status != m_status && waitTime > 0) {
                m_statusLock.wait(waitTime);
                long now = System.currentTimeMillis();
                waitTime -= (now - last);
            }
        
        }
    }

    protected void waitForStatus(int status) throws InterruptedException {
        synchronized (m_statusLock) {
            while (status != m_status) {
                m_statusLock.wait();
            }
        }
    }

    public int getStatus() {
        synchronized (m_statusLock) {
            return m_status;
        }
    }

    public String getStatusText() {
        return STATUS_NAMES[getStatus()];
    }

    public String status() {
        return getStatusText();
    }

    protected synchronized boolean isStartPending() {
        return getStatus() == START_PENDING;
    }

    protected synchronized boolean isRunning() {
        return getStatus() == RUNNING;
    }

    protected synchronized boolean isPaused() {
        return getStatus() == PAUSED;
    }

    protected synchronized boolean isStarting() {
        return getStatus() == STARTING;
    }

    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    protected void fatalf(String format, Object... args) {
        log().fatal(String.format(format, args));
    }

    protected void fatalf(Throwable t, String format, Object... args) {
        log().fatal(String.format(format, args), t);
    }

    protected void errorf(String format, Object... args) {
        log().error(String.format(format, args));
    }

    protected void errorf(Throwable t, String format, Object... args) {
        log().error(String.format(format, args), t);
    }

    protected void warnf(String format, Object... args) {
        log().warn(String.format(format, args));
    }

    protected void warnf(Throwable t, String format, Object... args) {
        log().warn(String.format(format, args), t);
    }

    protected void infof(String format, Object... args) {
        if (log().isInfoEnabled()) {
            log().info(String.format(format, args));
        }
    }

    protected void infof(Throwable t, String format, Object... args) {
        if (log().isInfoEnabled()) {
            log().info(String.format(format, args), t);
        }
    }

    protected void debugf(String format, Object... args) {
        if (log().isDebugEnabled()) {
            log().debug(String.format(format, args));
        }
    }

    protected void debugf(Throwable t, String format, Object... args) {
        if (log().isDebugEnabled()) {
            log().debug(String.format(format, args), t);
        }
    }

    final public void init() {
        String prefix = ThreadCategory.getPrefix();
        try {
            
            ThreadCategory.setPrefix(getName());
            log().debug(getName()+" initializing.");

            onInit();

            log().debug(getName()+" initialization complete.");
        } finally {
            ThreadCategory.setPrefix(prefix);
        }
    }



    final public void pause() {
        String prefix = ThreadCategory.getPrefix();
        try {
            ThreadCategory.setPrefix(getName());

            if (!isRunning())
                return;

            setStatus(PAUSE_PENDING);

            onPause();

            setStatus(PAUSED);

            log().debug(getName()+" paused.");

        } finally {
            ThreadCategory.setPrefix(prefix);
        }
    }

    final public void resume() {
        String prefix = ThreadCategory.getPrefix();
        try {
            ThreadCategory.setPrefix(getName());
            if (!isPaused())
                return;

            setStatus(RESUME_PENDING);

            onResume();

            setStatus(RUNNING);

            log().debug(getName()+" resumed.");
        } finally {
            ThreadCategory.setPrefix(prefix);
        }
    }

    final public synchronized void start() {
        String prefix = ThreadCategory.getPrefix();
        try {
            ThreadCategory.setPrefix(getName());
            log().debug(getName()+" starting.");

            setStatus(STARTING);

            onStart();

            setStatus(RUNNING);

            log().info(getName()+" started.");
        } finally {
            ThreadCategory.setPrefix(prefix);
        }

    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     */
    final public synchronized void stop() {
        String prefix = ThreadCategory.getPrefix();
        try {
            ThreadCategory.setPrefix(getName());
            log().debug(getName()+" stopping.");
            setStatus(STOP_PENDING);

            onStop();

            setStatus(STOPPED);

            log().info(getName()+" stopped.");
        } finally {
            ThreadCategory.setPrefix(prefix);
        }
    }

}
