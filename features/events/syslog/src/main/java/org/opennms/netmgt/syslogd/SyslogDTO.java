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

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.camel.MinionDTO;
import org.opennms.core.utils.InetAddressUtils;

@XmlRootElement(name="syslog-dto")
@XmlAccessorType(XmlAccessType.NONE)
public class SyslogDTO extends MinionDTO {

    public SyslogDTO() {
        super();
    }
	
//    @Override
//    public int hashCode() {
//        return Objects.hash(super.getHeaders(), super.getBody());
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        final SyslogDTO other = (SyslogDTO) obj;
//        return Objects.equals(super.getHeaders(), other.getHeaders())
//                && Arrays.equals(super.getBody(), other.getBody());
//    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("systemid", super.getHeaders().get(SYSTEM_ID))
            .append("location", super.getHeaders().get(LOCATION))
            .append("sourceaddress", super.getHeaders().get(SOURCE_ADDRESS))
            .append("sourceport", super.getHeaders().get(SOURCE_PORT))
            .append("body", super.getBody())
            .toString();
    }
    
	public void setSystemId(String m_systemId) {
		super.putIntoMap(MinionDTO.SYSTEM_ID, m_systemId);
	}

	public void setLocation(String m_location) {
		super.putIntoMap(MinionDTO.LOCATION, m_location);
	}

	public void setSourceAddress(InetAddress m_sourceAddress) {
		super.putIntoMap(MinionDTO.SOURCE_ADDRESS, InetAddressUtils.str(m_sourceAddress));
	}

	public void setSourceport(int m_sourceport) {
		super.putIntoMap(MinionDTO.SOURCE_PORT, String.valueOf(m_sourceport));
	}
	
}