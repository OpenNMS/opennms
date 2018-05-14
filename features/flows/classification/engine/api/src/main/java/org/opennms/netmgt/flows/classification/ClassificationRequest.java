/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification;

import java.util.Objects;

import org.opennms.netmgt.flows.classification.persistence.api.Protocol;

public class ClassificationRequest {

    private String location;
    private Protocol protocol;
    private int dstPort;
    private String dstAddress;
    private int srcPort;
    private String srcAddress;
    private String exporterAddress;

    public ClassificationRequest(String location, int dstPort, String dstAddress, Protocol protocol) {
        this.location = location;
        this.dstPort = dstPort;
        this.dstAddress = dstAddress;
        this.protocol = protocol;
    }

    public ClassificationRequest() {

    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public int getDstPort() {
        return dstPort;
    }

    public void setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
    }

    public String getDstAddress() {
        return dstAddress;
    }

    public int getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(int srcPort) {
        this.srcPort = srcPort;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public void setSrcAddress(String srcAddress) {
        this.srcAddress = srcAddress;
    }

    public String getExporterAddress() {
        return exporterAddress;
    }

    public void setExporterAddress(String exporterAddress) {
        this.exporterAddress = exporterAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassificationRequest that = (ClassificationRequest) o;
        boolean equals = Objects.equals(location, that.location)
                && Objects.equals(protocol, that.protocol)
                && Objects.equals(dstPort, that.dstPort)
                && Objects.equals(dstAddress, that.dstAddress)
                && Objects.equals(srcPort, that.srcPort)
                && Objects.equals(srcAddress, that.srcAddress)
                && Objects.equals(exporterAddress, that.exporterAddress);
        return equals;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, protocol, dstPort, dstAddress, srcPort, srcAddress, exporterAddress);
    }
}
