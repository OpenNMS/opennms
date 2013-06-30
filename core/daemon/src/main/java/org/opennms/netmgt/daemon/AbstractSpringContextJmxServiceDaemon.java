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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.Callable;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <p>Abstract AbstractSpringContextJmxServiceDaemon class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public abstract class AbstractSpringContextJmxServiceDaemon<T extends SpringServiceDaemon> implements BaseOnmsMBean {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractSpringContextJmxServiceDaemon.class);
	
    /** Constant <code>DAEMON_BEAN_NAME="daemon"</code> */
    public static final String DAEMON_BEAN_NAME = "daemon";

    private ClassPathXmlApplicationContext m_context;

    private int m_status = Fiber.START_PENDING;

    /**
     * <p>Constructor for AbstractSpringContextJmxServiceDaemon.</p>
     *
     * @param <T> a T object.
     */
    public AbstractSpringContextJmxServiceDaemon() {
        super();
    }
    
    /**
     * <p>getSpringContext</p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected abstract String getSpringContext();

    /**
     * <p>getLoggingPrefix</p>
     *
     * @return a {@link java.lang.String} object.
     */
    protected abstract String getLoggingPrefix();

    /**
     * This is here for unit tests to use.
     *
     * @return a {@link org.springframework.context.ApplicationContext} object.
     */
    protected ApplicationContext getContext() {
        return m_context;
    }
    
    /**
     * <p>init</p>
     */
    @Override
    public final void init() {
        Logging.withPrefix(getLoggingPrefix(), new Runnable() {

            @Override
            public void run() {
                LOG.info("{} initializing.", getLoggingPrefix());
                LOG.debug("SPRING: thread.classLoader={}", Thread.currentThread().getContextClassLoader());

                m_context = BeanUtils.getFactory(getSpringContext(), ClassPathXmlApplicationContext.class);

                LOG.debug("SPRING: context.classLoader= {}",  m_context.getClassLoader());
                LOG.info("{} initialization complete.", getLoggingPrefix());
            }
            
        });
    }
    
    /**
     * <p>start</p>
     */
    @Override
    public final void start() {
        
        Logging.withPrefix(getLoggingPrefix(), new Runnable() {

            @Override
            public void run() {
                LOG.info("{} initializing.", getLoggingPrefix());
                LOG.debug("SPRING: thread.classLoader={}", Thread.currentThread().getContextClassLoader());

                setStatus(Fiber.STARTING);
                SpringServiceDaemon daemon = getDaemon();
                try {
                    daemon.start();
                } catch (Throwable t) {
                    LOG.error("Could not start daemon: {}", t, t);
                    
                    try {
                        stop();
                    } catch (Throwable tt) {
                        LOG.error("Could not stop daemon after it failed to start: {}", tt, tt);
                    }
                    
                    if (t instanceof RuntimeException) {
                        throw (RuntimeException) t;
                    } else {
                        throw new UndeclaredThrowableException(t);
                    }
                }
                setStatus(Fiber.RUNNING);

                LOG.debug("SPRING: context.classLoader= {}",  m_context.getClassLoader());
                LOG.info("{} initialization complete.", getLoggingPrefix());
            }
            
        });
        
    }

    /**
     * Get the service daemon object that this JMX MBean represents.
     *
     * @return the service daemon object
     */
    @SuppressWarnings("unchecked")
    public T getDaemon() {
        return (T) m_context.getBean(DAEMON_BEAN_NAME, SpringServiceDaemon.class);
    }

    /**
     * <p>stop</p>
     */
    @Override
    public final void stop() {
        Logging.withPrefix(getLoggingPrefix(), new Runnable() {

            @Override
            public void run() {
                
                setStatus(Fiber.STOP_PENDING);

                if (m_context != null) {
                    m_context.close();
                }
                
                setStatus(Fiber.STOPPED);
            }
            
        });
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public final int getStatus() {
        return m_status;
    }

    private void setStatus(int status) {
        m_status = status;
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String status() {
        try {
            return Logging.withPrefix(getLoggingPrefix(), new Callable<String>() {

                @Override
                public String call() {
                    return Fiber.STATUS_NAMES[getStatus()];
                }

            });
        } catch (Exception e) {
            LOG.error("An exception occurred retrieving status for {}", getLoggingPrefix());
            return "failed";
        }
    }

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public final String getStatusText() {
        return status();
    }

}
