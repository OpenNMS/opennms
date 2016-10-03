/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.camel.MinionDTO;

@XmlRootElement(name = "syslog-dto")
@XmlAccessorType(XmlAccessType.NONE)
public class SyslogDTO extends MinionDTO {

	public SyslogDTO() {
		// No-arg constructor for JAXB
		super();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("systemId", super.getHeaders().get(SYSTEM_ID))
				.append("location", super.getHeaders().get(LOCATION))
				.append("sourceAddress", super.getHeaders().get(SOURCE_ADDRESS))
				.append("sourcePort", super.getHeaders().get(SOURCE_PORT))
				.append("body", super.getBody()).toString();
	}

}
