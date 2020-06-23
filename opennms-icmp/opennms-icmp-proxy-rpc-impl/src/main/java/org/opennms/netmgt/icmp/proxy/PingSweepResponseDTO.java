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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RpcResponse;

@XmlRootElement(name = "ping-sweep-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class PingSweepResponseDTO implements RpcResponse {

    @XmlAttribute(name="error")
    private String error;

    @XmlElement(name = "pinger-result")
    private List<PingSweepResultDTO> pingSweepResult = new ArrayList<>(0);

    public PingSweepResponseDTO() { }

    public PingSweepResponseDTO(Throwable ex) {
        error = RemoteExecutionException.toErrorMessage(ex);
    }

    public List<PingSweepResultDTO> getPingSweepResult() {
        return pingSweepResult;
    }

    public void setPingSweepResult(List<PingSweepResultDTO> pingSweepResult) {
        this.pingSweepResult = pingSweepResult;
    }

    public void addPingSweepResult(PingSweepResultDTO pingSweepResult) {
        this.pingSweepResult.add(pingSweepResult);
    }

    @Override
    public String getErrorMessage() {
        return error;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pingSweepResult, error);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PingSweepResponseDTO other = (PingSweepResponseDTO) obj;
        return Objects.equals(this.pingSweepResult, other.pingSweepResult) &&
                Objects.equals(this.error, other.error);
    }

}
