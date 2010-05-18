/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2010 Feb 02: Fixed bug #2479: NPE seen if stop() is called without init().
 *              - dj@opennms.org
 * 2008 Jul 29: Genericized with the class of the service daemon and added a
 *              getDaemon() method to return the daemon. - dj@opennms.org
 * 
 * Created: March 30, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public abstract class AbstractSpringContextJmxServiceDaemon<T extends SpringServiceDaemon> implements BaseOnmsMBean {

    public static final String DAEMON_BEAN_NAME = "daemon";

    private ClassPathXmlApplicationContext m_context;

    private int m_status = Fiber.START_PENDING;

    public AbstractSpringContextJmxServiceDaemon() {
        super();
    }
    
    protected abstract String getSpringContext();

    protected abstract String getLoggingPrefix();

    /**
     * This is here for unit tests to use.
     */
    protected ApplicationContext getContext() {
        return m_context;
    }
    
    public final void init() {
        setLoggingCategory();
  
        log().debug("SPRING: thread.classLoader=" + Thread.currentThread().getContextClassLoader());

        m_context = BeanUtils.getFactory(getSpringContext(), ClassPathXmlApplicationContext.class);

        log().debug("SPRING: context.classLoader= "+ m_context.getClassLoader());
    }
    
    public final void start() {
        setLoggingCategory();
        
        setStatus(Fiber.STARTING);
        SpringServiceDaemon daemon = getDaemon();
        try {
            daemon.start();
        } catch (Throwable t) {
            log().error("Could not start daemon: " + t, t);
            
            try {
                stop();
            } catch (Throwable tt) {
                log().error("Could not stop daemon after it failed to start: " + tt, tt);
            }
            
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new UndeclaredThrowableException(t);
            }
        }
        setStatus(Fiber.RUNNING);
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

    public final void stop() {
        setLoggingCategory();
        
        setStatus(Fiber.STOP_PENDING);

        if (m_context != null) {
            m_context.close();
        }
        
        setStatus(Fiber.STOPPED);
    }

    public final int getStatus() {
        return m_status;
    }

    private void setStatus(int status) {
        m_status = status;
    }

    public final String status() {
        setLoggingCategory();
        
        return Fiber.STATUS_NAMES[getStatus()];
    }

    public final String getStatusText() {
        return status();
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance();
    }

    private void setLoggingCategory() {
        ThreadCategory.setPrefix(getLoggingPrefix());
    }
}
