/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.icmp.proxy;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RpcResponse;

@XmlRootElement(name="ping-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class PingResponseDTO implements RpcResponse {

    @XmlAttribute(name="error")
    private String error;

    @XmlElement(name="rtt") // milliseconds
    private double rtt;

    public PingResponseDTO() { }

    public PingResponseDTO(Throwable ex) {
        error = RemoteExecutionException.toErrorMessage(ex);
    }

    public double getRtt() {
        return rtt;
    }

    public void setRtt(double rtt) {
        this.rtt = rtt;
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final PingResponseDTO that = (PingResponseDTO) o;
        return Objects.equals(rtt, that.rtt) &&
                Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rtt, error);
    }
}
