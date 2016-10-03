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

package org.opennms.netmgt.syslogd;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogObjectToDTOProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(SyslogObjectToDTOProcessor.class);

	public static final String INCLUDE_RAW_MESSAGE = "includeRawMessage";
	public static final boolean INCLUDE_RAW_MESSAGE_DEFAULT = Boolean.TRUE;

	@Override
	public void process(final Exchange exchange) throws Exception {
		final SyslogConnection object = exchange.getIn().getBody(SyslogConnection.class);
		boolean syslogRawMessageFlag = (boolean)exchange.getIn().getHeader(INCLUDE_RAW_MESSAGE);
		exchange.getIn().setBody(object2dto(object, syslogRawMessageFlag), SyslogDTO.class);
	}

	public static SyslogDTO object2dto(SyslogConnection syslog) {
		return object2dto(syslog, INCLUDE_RAW_MESSAGE_DEFAULT);
	}

	public static SyslogDTO object2dto(SyslogConnection syslog, boolean syslogRawMessageFlag) {

		SyslogDTO syslogDTO = new SyslogDTO();
		syslogDTO.setLocation(syslog.getLocation());
		syslogDTO.setSourceAddress(syslog.getSourceAddress());
		syslogDTO.setSourcePort(syslog.getPort());
		syslogDTO.setSystemId(syslog.getSystemId());

		if(syslogRawMessageFlag){
			syslogDTO.setBody(syslog.getBytes());
		}

		return syslogDTO;
	}
}
