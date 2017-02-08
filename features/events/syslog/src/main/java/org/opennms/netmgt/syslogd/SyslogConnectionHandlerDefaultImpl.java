/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.opennms.core.concurrent.ExecutorFactory;
import org.opennms.core.concurrent.ExecutorFactoryJavaImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogConnectionHandlerDefaultImpl implements SyslogConnectionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(SyslogConnectionHandlerDefaultImpl.class);

	/**
	 * This is the number of threads that are used to parse syslog messages into
	 * OpenNMS events.
	 * 
	 * TODO: Make this configurable
	 */
	public static final int EVENT_PARSER_THREADS = Runtime.getRuntime().availableProcessors();

	/**
	 * This is the number of threads that are used to broadcast the OpenNMS events.
	 * 
	 * TODO: Make this configurable
	 */
	public static final int EVENT_SENDER_THREADS = Runtime.getRuntime().availableProcessors();

	private final ExecutorFactory m_executorFactory = new ExecutorFactoryJavaImpl();
	private final ExecutorService m_syslogConnectionExecutor = m_executorFactory.newExecutor(EVENT_PARSER_THREADS, Integer.MAX_VALUE, "OpenNMS.Syslogd", "syslogConnections");
	private final ExecutorService m_syslogProcessorExecutor = m_executorFactory.newExecutor(EVENT_SENDER_THREADS, Integer.MAX_VALUE, "OpenNMS.Syslogd", "syslogProcessors");

	@Override
	public void handleSyslogConnection(final SyslogConnection message) {
		try {
			CompletableFuture.supplyAsync(message::call, m_syslogConnectionExecutor)
				.thenAcceptAsync(proc -> proc.call(), m_syslogProcessorExecutor);
		} catch (Throwable e) {
			LOG.error("Task execution failed in {}", this.getClass().getSimpleName(), e);
		}
	}
}
