/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.support;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.IOException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

@Plugin(name = "String", category = "OpenNMS", elementType = "appender", printObject = true)
public class Log4j2StringAppender extends AbstractOutputStreamAppender<Log4j2StringAppender.ByteArrayOutputStreamManager> {

	private static final long serialVersionUID = -6024745534963187041L;

	private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();

	private final static LoggerContext LOGGER_CONTEXT = (LoggerContext)LogManager.getContext(false);
	private final static Configuration LOGGER_CONTEXT_CONFIGURATION = LOGGER_CONTEXT.getConfiguration();

	/**
	 * <p>Create an appender with the following pattern as the layout (to match our log file layout):</p>
	 * 
	 * {@code %d %-5p [%t] %c{1.}: %m%n}
	 * 
	 * @param name
	 * @param filter
	 * @param ignoreExceptions
	 * @param immediateFlush
	 */
	private Log4j2StringAppender(String name, Filter filter, boolean ignoreExceptions, boolean immediateFlush) {
		this(name, PatternLayout.newBuilder().withPattern("%d %-5p [%t] %c{1.}: %m%n").build(), filter, ignoreExceptions, immediateFlush);
	}

	/**
	 * Create a new {@link Log4j2StringAppender}.
	 * 
	 * @param name
	 * @param layout
	 * @param filter
	 * @param ignoreExceptions
	 * @param immediateFlush
	 */
	private Log4j2StringAppender(String name, Layout<? extends Serializable> layout, Filter filter, boolean ignoreExceptions, boolean immediateFlush) {
		super(name, layout, filter, ignoreExceptions, immediateFlush, new ByteArrayOutputStreamManager(layout));
	}

	@PluginFactory
	public static Log4j2StringAppender createAppender() {
		// Make a unique name for the Appender
		String name = Log4j2StringAppender.class.getName() + INSTANCE_COUNTER.getAndIncrement();
		return new Log4j2StringAppender(name, null, false, true);
	}

	public void addToLogger(String loggerName, Level level) {
		LoggerConfig loggerConfig = LOGGER_CONTEXT_CONFIGURATION.getLoggerConfig(loggerName);
		// Make sure the logger accepts messages at the given level
		if (level.isLessSpecificThan(loggerConfig.getLevel())) {
		    loggerConfig.setLevel(level);
		}
		loggerConfig.addAppender(this, level, null);
		LOGGER_CONTEXT.updateLoggers();
	}

	public void removeFromLogger(String loggerName) {
		LoggerConfig loggerConfig = LOGGER_CONTEXT_CONFIGURATION.getLoggerConfig(loggerName);
		loggerConfig.removeAppender(getName());
		LOGGER_CONTEXT.updateLoggers();
	}

	public String getOutput() {
		try {
			getManager().flush();
			return new String(getManager().getByteArrayOutputStream().toByteArray());
		} catch (IOException e) {
			return new String();
		}
	}

	public static class ByteArrayOutputStreamManager extends OutputStreamManager {
		protected ByteArrayOutputStreamManager(Layout<?> layout) {
			super(new ByteArrayOutputStream(), ByteArrayOutputStreamManager.class.getName(), layout, true);
		}

		public ByteArrayOutputStream getByteArrayOutputStream() throws IOException {
			return (ByteArrayOutputStream)getOutputStream();
		}
	}
}
