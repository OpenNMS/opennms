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

package org.opennms.netmgt.trapd;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.opennms.core.concurrent.ExecutorFactory;
import org.opennms.core.concurrent.ExecutorFactoryJavaImpl;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.snmp.TrapNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrapNotificationHandlerDefaultImpl implements TrapNotificationHandler {
	private static final Logger LOG = LoggerFactory.getLogger(TrapNotificationHandlerDefaultImpl.class);

	/**
	 * This is the number of threads that are used to process traps.
	 * 
	 * TODO: Make this configurable
	 */
	public static final int TRAP_PROCESSOR_THREADS = Runtime.getRuntime().availableProcessors();

	private final ExecutorFactory m_executorFactory = new ExecutorFactoryJavaImpl();
	private final ExecutorService m_processorExecutor = m_executorFactory.newExecutor(TRAP_PROCESSOR_THREADS, Integer.MAX_VALUE, "OpenNMS.Trapd", "trapProcessors");

	private TrapQueueProcessorFactory m_processorFactory;

	private InterfaceToNodeCache m_cache;

	@Override
	public void handleTrapNotification(final TrapNotification message) {
		try {
			// HZN-632: Call message.setProcessor() to change the processor to an EventCreator
			message.setTrapProcessor(new EventCreator(m_cache));
			// Use the TrapQueueProcessorFactory to construct a TrapQueueProcessor
			TrapQueueProcessor processor = m_processorFactory.getInstance(message);
			// Call the processor asynchronously
			CompletableFuture.supplyAsync(processor::call, m_processorExecutor);
		} catch (Throwable e) {
			LOG.error("Task execution failed in {}", this.getClass().getSimpleName(), e);
		}
	}

	/**
	 * @return the m_processorFactory
	 */
	public TrapQueueProcessorFactory getProcessorFactory() {
		return m_processorFactory;
	}

	/**
	 * @param m_processorFactory the m_processorFactory to set
	 */
	public void setProcessorFactory(TrapQueueProcessorFactory m_processorFactory) {
		this.m_processorFactory = m_processorFactory;
	}

	/**
	 * @return the trapdIpManager
	 */
	public InterfaceToNodeCache getInterfaceToNodeCache() {
		return m_cache;
	}

	/**
	 * @param cache the InterfaceToNodeCache to set
	 */
	public void setInterfaceToNodeCache(InterfaceToNodeCache cache) {
		m_cache = cache;
	}

}
