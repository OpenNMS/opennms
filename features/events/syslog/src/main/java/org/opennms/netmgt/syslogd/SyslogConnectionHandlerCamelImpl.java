/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import org.apache.camel.InOnly;
import org.apache.camel.Produce;
import org.opennms.core.camel.DefaultDispatcher;

/**
 * This class is an {@link InOnly} endpoint that will send messages to the 
 * Camel endpoint specified by the <code>endpointUri</code> constructor argument.
 */
public class SyslogConnectionHandlerCamelImpl extends DefaultDispatcher implements SyslogConnectionHandler {

	@Produce(property="endpointUri")
	SyslogConnectionHandler m_proxy;

	public SyslogConnectionHandlerCamelImpl(final String endpointUri) {
		super(endpointUri);
	}

	/**
	 * Send the incoming {@link SyslogConnection} message into the Camel route
	 * specified by the {@link #m_endpointUri} property.
	 */
	@Override
	public void handleSyslogConnection(final SyslogConnection message) {
		m_proxy.handleSyslogConnection(message);
	}
}
