/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.daemon;

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.logging.Logging;
import org.opennms.core.spring.BeanUtils;
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
        Logging.withPrefix(getLoggingPrefix(), () -> {
            LOG.info("{} initializing.", getLoggingPrefix());
            LOG.debug("SPRING: thread.classLoader={}", Thread.currentThread().getContextClassLoader());

            m_context = BeanUtils.getFactory(getSpringContext(), ClassPathXmlApplicationContext.class);

            LOG.debug("SPRING: context.classLoader= {}",  m_context.getClassLoader());
            LOG.info("{} initialization complete.", getLoggingPrefix());
        });
    }
    
    /**
     * <p>start</p>
     */
    @SuppressWarnings("java:S1181")
    @Override
    public final void start() {
        
        Logging.withPrefix(getLoggingPrefix(), () -> {
            LOG.info("{} starting.", getLoggingPrefix());
            LOG.debug("SPRING: thread.classLoader={}", Thread.currentThread().getContextClassLoader());

            setStatus(Fiber.STARTING);
            SpringServiceDaemon daemon = getDaemon();
            try {
                daemon.start();
            } catch (final Throwable t) {
                LOG.error("Could not start daemon", t);

                try {
                    stop();
                } catch (final Throwable tt) {
                    LOG.error("Could not stop daemon after it failed to start", tt);
                }

                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new UndeclaredThrowableException(t);
                }
            }
            setStatus(Fiber.RUNNING);

            LOG.debug("SPRING: context.classLoader= {}",  m_context.getClassLoader());
            LOG.info("{} starting complete.", getLoggingPrefix());
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
        Logging.withPrefix(getLoggingPrefix(), () -> {
            setStatus(Fiber.STOP_PENDING);

            if (m_context != null) {
                m_context.close();
            }
            
            setStatus(Fiber.STOPPED);
        });
    }

    @Override
    public final int getStatus() {
        return m_status;
    }

    private void setStatus(int status) {
        m_status = status;
    }

    @Override
    public final String status() {
        try {
            return Logging.withPrefix(getLoggingPrefix(), () -> Fiber.STATUS_NAMES[getStatus()]);
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
