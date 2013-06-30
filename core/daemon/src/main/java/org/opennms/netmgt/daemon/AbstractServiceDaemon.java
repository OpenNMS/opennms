/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.daemon;

import org.opennms.core.logging.Logging;
import org.opennms.netmgt.model.ServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * <p>Abstract AbstractServiceDaemon class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public abstract class AbstractServiceDaemon implements ServiceDaemon, SpringServiceDaemon {
	
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceDaemon.class);
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public final void afterPropertiesSet() throws Exception {
        init();
    }

    /**
     * The current status of this fiber
     */
    private int m_status;

    private String m_name;
    
    private Object m_statusLock = new Object();

    /**
     * <p>onInit</p>
     */
    abstract protected void onInit();

    /**
     * <p>onPause</p>
     */
    protected void onPause() {}

    /**
     * <p>onResume</p>
     */
    protected void onResume() {}

    /**
     * <p>onStart</p>
     */
    protected void onStart() {}

    /**
     * <p>onStop</p>
     */
    protected void onStop() {}

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    final public String getName() { return m_name; }

    /**
     * <p>Constructor for AbstractServiceDaemon.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    protected AbstractServiceDaemon(final String name) {
        m_name = name;
        setStatus(START_PENDING);
    }

    /**
     * <p>setStatus</p>
     *
     * @param status a int.
     */
    protected final void setStatus(final int status) {
        synchronized (m_statusLock) {
            m_status = status;
            m_statusLock.notifyAll();
        }
    }
    
    /**
     * <p>waitForStatus</p>
     *
     * @param status a int.
     * @param timeout a long.
     * @throws java.lang.InterruptedException if any.
     */
    protected final void waitForStatus(final int status, final long timeout) throws InterruptedException {
        synchronized (m_statusLock) {
            
            final long last = System.currentTimeMillis();
            long waitTime = timeout;
            while (status != m_status && waitTime > 0) {
                m_statusLock.wait(waitTime);
                long now = System.currentTimeMillis();
                waitTime -= (now - last);
            }
        
        }
    }

    /**
     * <p>waitForStatus</p>
     *
     * @param status a int.
     * @throws java.lang.InterruptedException if any.
     */
    protected final void waitForStatus(final int status) throws InterruptedException {
        synchronized (m_statusLock) {
            while (status != m_status) {
                m_statusLock.wait();
            }
        }
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        synchronized (m_statusLock) {
            return m_status;
        }
    }

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStatusText() {
        return STATUS_NAMES[getStatus()];
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     * 
     * @deprecated Use {@link #getStatusText()} instead. This field is only for 
     * backwards compatibility with JMX operations.
     */
    public String status() {
        return getStatusText();
    }

    /**
     * <p>isRunning</p>
     *
     * @return a boolean.
     */
    protected synchronized boolean isRunning() {
        return getStatus() == RUNNING;
    }

    /**
     * <p>isPaused</p>
     *
     * @return a boolean.
     */
    protected synchronized boolean isPaused() {
        return getStatus() == PAUSED;
    }

    /**
     * <p>isStarting</p>
     *
     * @return a boolean.
     */
    protected synchronized boolean isStarting() {
        return getStatus() == STARTING;
    }
    
    /**
     * <p>init</p>
     */
    final public void init() {
        
        Logging.withPrefix(getName(), new Runnable() {

            @Override
            public void run() {
                LOG.info("{} initializing.", getName());

                onInit();

                LOG.info("{} initialization complete.", getName());
            }
            
        });
    }



    /**
     * <p>pause</p>
     */
    @Override
    final public void pause() {
        Logging.withPrefix(getName(), new Runnable() {

            @Override
            public void run() {
                if (!isRunning()) return;

                LOG.info("{} pausing.", getName());

                setStatus(PAUSE_PENDING);
                onPause();
                setStatus(PAUSED);

                LOG.info("{} paused.", getName());
            }
            
        });
    }

    /**
     * <p>resume</p>
     */
    @Override
    final public void resume() {
        
        Logging.withPrefix(getName(), new Runnable() {

            @Override
            public void run() {
                if (!isPaused()) return;

                LOG.info("{} resuming.", getName());

                setStatus(RESUME_PENDING);
                onResume();
                setStatus(RUNNING);

                LOG.info("{} resumed.", getName());
            }
            
        });
    }


    /**
     * <p>start</p>
     */
    @Override
    final public synchronized void start() {
        
        Logging.withPrefix(getName(), new Runnable() {

            @Override
            public void run() {
                LOG.info("{} starting.", getName());

                setStatus(STARTING);
                onStart();
                setStatus(RUNNING);

                LOG.info("{} started.", getName());
            }
            
        });
        
    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     */
    @Override
    final public synchronized void stop() {
        
        Logging.withPrefix(getName(), new Runnable() {

            @Override
            public void run() {
                LOG.info("{} stopping.", getName());

                setStatus(STOP_PENDING);
                onStop();
                setStatus(STOPPED);

                LOG.info("{} stopped.", getName());
            }
            
        });
        
    }

    /**
     * Destroys the current service.
     */
    @Override
    final public void destroy() {
        stop();
    }

}
