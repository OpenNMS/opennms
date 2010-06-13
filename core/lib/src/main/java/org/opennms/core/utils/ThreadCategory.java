//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 23: Eliminate deprecated method. - dj@opennms.org
// 2007 May 12: Dedeplicate and use Java 5 generics. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
//
// Tab Size = 8
//

package org.opennms.core.utils;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * This class is designed to work with log4j based on threads and not class
 * names. This allows all the classes invoked by a thread to log their messages
 * to the same location. This is particularly useful when messages from share
 * common code should be associated with a higher level <EM>service</EM> or
 * <EM>application</EM>.
 * 
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class ThreadCategory {
    public enum Level {
        FATAL,
        ERROR,
        WARN,
        INFO,
        DEBUG,
        TRACE,
        ALL,
        OFF
    }

    private static final String DEFAULT_CATEGORY = "UNCATEGORIZED";

    private String messagePrefix = null;

    private final Logger m_delegate;

    /**
     * This thread local variable is used to store the category that threads
     * (and their children) should use to log information. If a thread has a set
     * category and then starts a new thread, the new thread will inherit the
     * category of the parent thread.
     */
    private static InheritableThreadLocal<String> s_threadCategory = new InheritableThreadLocal<String>();

    /**
     * This constructor creates a new ThreadCategory instance.
     * 
     * @param name
     *            The name of the category
     * 
     */
    protected ThreadCategory(String name) {
        m_delegate = Logger.getLogger(name);
    }

    /**
     * This method is used to get the category instance associated with the
     * thread. If the category for the thread has not been set then the passed
     * class is used to find the appropriate category. If a category is found
     * for the thread group then it is returned to the caller.
     * 
     * @param c
     *            The class used to find the category if it was not set.
     * 
     * @return The instance for the thread.
     * 
     * @see java.lang.InheritableThreadLocal
     */
    public static ThreadCategory getInstance(Class<?> c) {
    	return getLog4jInstance(c);
    }

    public static ThreadCategory getLog4jInstance(Class<?> c) {
        return getLog4jInstance(c.getName());
    }
    
    public static org.slf4j.Logger getSlf4jInstance(Class<?> c) {
        return getSlf4jInstance(c.getName());
    }

    /**
     * This method is used to get the category instance associated with the
     * thread. If the category for the thread has not been set then the passed
     * name is used to find the appropriate category. If a category is found for
     * the thread group then it is returned to the caller.
     * 
     * @param cname
     *            The name used to find the category if it was not set.
     * 
     * @return The instance for the thread.
     * 
     * @see java.lang.InheritableThreadLocal
     */
    public static ThreadCategory getInstance(String cname) {
        return getLog4jInstance(cname);
    }
    
    public static ThreadCategory getLog4jInstance(String cname) {
        String prefix = getPrefix();

        if ((prefix != null) && !prefix.equals("")) {
            return new ThreadCategory(prefix + "." + cname);
        } else {
            return new ThreadCategory(cname);
        }
    }

    public static org.slf4j.Logger getSlf4jInstance(String cname) {
        String prefix = getPrefix();

        if ((prefix != null) && !prefix.equals("")) {
            return org.slf4j.LoggerFactory.getLogger(prefix + "." + cname);
        } else {
            return org.slf4j.LoggerFactory.getLogger(cname);
        }
    }
    
    /**
     * This method is used to get the category instance associated with the
     * thread. If the instance has not been set then a default category is
     * returned to the caller.
     * 
     * @return The instance for the thread, null if it is not set.
     * 
     * @see java.lang.InheritableThreadLocal
     */
    public static ThreadCategory getInstance() {
        return getLog4jInstance();
    }
    
    public static ThreadCategory getLog4jInstance() {
        String prefix = getPrefix();
        
        if (prefix != null) {
            return new ThreadCategory(prefix);
        } else {
            /*
             * Use the default category anywhere that ThreadCategory
             * is instantiated without a prefix, classname, or user-
             * specified string.
             */
            return new ThreadCategory(DEFAULT_CATEGORY);
        }
    }
    
    public static org.slf4j.Logger getSlf4jInstance() {
        String prefix = getPrefix();
        
        if (prefix != null) {
            return org.slf4j.LoggerFactory.getLogger(prefix);
        } else {
            return org.slf4j.LoggerFactory.getLogger(DEFAULT_CATEGORY);
        }
    }

    /**
     * This method is used to set a prefix for the category name that is used
     * for all category instances in this thread. This is used to insure that
     * all messages from a particular thread end up in the same log file,
     * regardless of the package or class name of the class that generated the
     * log message. Please restrict the usage of this function to only the
     * highest level threads.
     */
    public static void setPrefix(String prefix) {
        s_threadCategory.set(prefix);
    }

    /**
     * This is used to retrieve the current prefix as it has been inherited by
     * the calling thread. This is needed by many dynamic threading classes like
     * the {@link RunnableConsumerThreadPool} to ensure that all the
     * internal threads run in the same category.
     * 
     * @return The prefix string as inherited by the calling thread.
     */
    public static String getPrefix() {
        return s_threadCategory.get();
    }

	/**
	 * @param message
	 * @param t
	 * @see org.apache.log4j.Category#debug(java.lang.Object, java.lang.Throwable)
	 */
	public void debug(String message, Throwable t) {
		m_delegate.debug(messagePrefix == null ? message : messagePrefix + message, t);
	}

	/**
	 * @param message
	 * @see org.apache.log4j.Category#debug(java.lang.Object)
	 */
	public void debug(String message) {
		m_delegate.debug(messagePrefix == null ? message : messagePrefix + message);
	}

	/**
	 * @param message
	 * @param t
	 * @see org.apache.log4j.Category#error(java.lang.Object, java.lang.Throwable)
	 */
	public void error(String message, Throwable t) {
		m_delegate.error(messagePrefix == null ? message : messagePrefix + message, t);
	}

	/**
	 * @param message
	 * @see org.apache.log4j.Category#error(java.lang.Object)
	 */
	public void error(String message) {
		m_delegate.error(messagePrefix == null ? message : messagePrefix + message);
	}

	/**
	 * @param message
	 * @param t
	 * @see org.apache.log4j.Category#fatal(java.lang.Object, java.lang.Throwable)
	 */
	public void fatal(String message, Throwable t) {
		m_delegate.fatal(messagePrefix == null ? message : messagePrefix + message, t);
	}

	/**
	 * @param message
	 * @see org.apache.log4j.Category#fatal(java.lang.Object)
	 */
	public void fatal(String message) {
		m_delegate.fatal(messagePrefix == null ? message : messagePrefix + message);
	}

	/**
	 * @see org.apache.log4j.Category#info(java.lang.Object, java.lang.Throwable)
	 */
	public void info(String message, Throwable t) {
		m_delegate.info(messagePrefix == null ? message : messagePrefix + message, t);
	}

	/**
	 * @see org.apache.log4j.Category#info(java.lang.Object)
	 */
	public void info(String message) {
		// m_delegate.info(messagePrefix == null ? message : messagePrefix + message);
		m_delegate.info(message);
	}

	/**
	 * @param message
	 * @param t
	 * @see org.apache.log4j.Logger#trace(java.lang.Object, java.lang.Throwable)
	 */
	public void trace(String message, Throwable t) {
		m_delegate.trace(messagePrefix == null ? message : messagePrefix + message, t);
	}

	/**
	 * @param message
	 * @see org.apache.log4j.Logger#trace(java.lang.Object)
	 */
	public void trace(String message) {
		m_delegate.trace(messagePrefix == null ? message : messagePrefix + message);
	}

	/**
	 * @param message
	 * @param t
	 * @see org.apache.log4j.Category#warn(java.lang.Object, java.lang.Throwable)
	 */
	public void warn(String message, Throwable t) {
		m_delegate.warn(message, t);
	}

	/**
	 * @param message
	 * @see org.apache.log4j.Category#warn(java.lang.Object)
	 */
	public void warn(String message) {
		m_delegate.warn(message);
	}

	/**
	 * @return the messagePrefix
	 */
	public String getMessagePrefix() {
		return messagePrefix;
	}

	/**
	 */
	public void clearMessagePrefix() {
		messagePrefix = null;
	}

	/**
	 * @param messagePrefix the messagePrefix to set
	 */
	public void setMessagePrefix(String messagePrefix) {
		this.messagePrefix = messagePrefix;
	}

	/**
	 * @return
	 * @see org.apache.log4j.Category#isDebugEnabled()
	 */
	public boolean isDebugEnabled() {
		return m_delegate.isDebugEnabled();
	}

	/**
	 * @param level
	 * @return
	 * @see org.apache.log4j.Category#isEnabledFor(org.apache.log4j.Priority)
	 */
	public boolean isEnabledFor(Level level) {
		switch(level) {
		case FATAL:
			return m_delegate.isEnabledFor(org.apache.log4j.Level.FATAL);
		case ERROR:
			return m_delegate.isEnabledFor(org.apache.log4j.Level.ERROR);
		case WARN:
			return m_delegate.isEnabledFor(org.apache.log4j.Level.WARN);
		case INFO:
			return m_delegate.isEnabledFor(org.apache.log4j.Level.INFO);
		case DEBUG:
			return m_delegate.isEnabledFor(org.apache.log4j.Level.DEBUG);
		case TRACE:
			return m_delegate.isEnabledFor(org.apache.log4j.Level.TRACE);
		case ALL:
			return m_delegate.isEnabledFor(org.apache.log4j.Level.ALL);
		case OFF:
			return m_delegate.isEnabledFor(org.apache.log4j.Level.OFF);
		default:
			throw new IllegalArgumentException("Invalid logging level: " + level);
		}
	}

	/**
	 * @return
	 * @see org.apache.log4j.Category#isInfoEnabled()
	 */
	public boolean isInfoEnabled() {
		return m_delegate.isInfoEnabled();
	}

	/**
	 * @return
	 * @see org.apache.log4j.Logger#isTraceEnabled()
	 */
	public boolean isTraceEnabled() {
		return m_delegate.isTraceEnabled();
	}

	/**
	 * @return
	 * @see org.apache.log4j.Category#getName()
	 */
	public final String getName() {
		return m_delegate.getName();
	}

	/**
	 * @return
	 * @see org.apache.log4j.Category#getLevel()
	 */
	public final Level getLevel() {
		switch(m_delegate.getLevel().toInt()) {
		case org.apache.log4j.Level.FATAL_INT:
			return Level.FATAL;
		case org.apache.log4j.Level.ERROR_INT:
			return Level.ERROR;
		case org.apache.log4j.Level.WARN_INT:
			return Level.WARN;
		case org.apache.log4j.Level.INFO_INT:
			return Level.INFO;
		case org.apache.log4j.Level.DEBUG_INT:
			return Level.DEBUG;
		case org.apache.log4j.Level.TRACE_INT:
			return Level.TRACE;
		case org.apache.log4j.Level.ALL_INT:
			return Level.ALL;
		case org.apache.log4j.Level.OFF_INT:
			return Level.OFF;
		default:
			throw new IllegalStateException("Invalid logging level set: " + m_delegate.getLevel());
		}
	}

	/**
	 * @param level
	 * @see org.apache.log4j.Category#setLevel(org.apache.log4j.Level)
	 */
	public void setLevel(Level level) {
		switch(level) {
		case FATAL:
			m_delegate.setLevel(org.apache.log4j.Level.FATAL);
			break;
		case ERROR:
			m_delegate.setLevel(org.apache.log4j.Level.ERROR);
			break;
		case WARN:
			m_delegate.setLevel(org.apache.log4j.Level.WARN);
			break;
		case INFO:
			m_delegate.setLevel(org.apache.log4j.Level.INFO);
			break;
		case DEBUG:
			m_delegate.setLevel(org.apache.log4j.Level.DEBUG);
			break;
		case TRACE:
			m_delegate.setLevel(org.apache.log4j.Level.TRACE);
			break;
		case ALL:
			m_delegate.setLevel(org.apache.log4j.Level.ALL);
			break;
		case OFF:
			m_delegate.setLevel(org.apache.log4j.Level.OFF);
			break;
		}
	}
}
