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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.opennms.core.concurrent.WaterfallExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogConnectionHandlerDefaultImpl implements SyslogConnectionHandler {
	private static final Logger LOG = LoggerFactory.getLogger(SyslogReceiverJavaNetImpl.class);

	private final ExecutorService m_executor;

	public SyslogConnectionHandlerDefaultImpl(ExecutorService executor) {
		m_executor = executor;
	}

	/**
	 * Send the incoming {@link SyslogConnection} message into the Camel route
	 * specified by the {@link #m_endpointUri} property.
	 */
	@Override
	public void handleSyslogConnection(final SyslogConnection message) {
		//SyslogConnection *Must* copy packet data and InetAddress as DatagramPacket is a mutable type
		try {
			WaterfallExecutor.waterfall(m_executor, message);
		} catch (ExecutionException e) {
			LOG.error("Task execution failed in {}", this.getClass().getSimpleName(), e);
		} catch (InterruptedException e) {
			LOG.error("Task interrupted in {}", this.getClass().getSimpleName(), e);
		}
	}
}
