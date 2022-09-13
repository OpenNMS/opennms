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

public class ClassificationRequest {

    private String location;
    private Integer protocol;
    private Integer dstPort;
    private IpAddr dstAddress;
    private Integer srcPort;
    private IpAddr srcAddress;
    private IpAddr exporterAddress;

    public ClassificationRequest(final String location,
                                 final int srcPort,
                                 final IpAddr srcAddress,
                                 final int dstPort,
                                 final IpAddr dstAddress,
                                 final int protocol,
                                 final IpAddr exporterAddress) {
        this.location = location;
        this.srcPort = srcPort;
        this.srcAddress = srcAddress;
        this.dstPort = dstPort;
        this.dstAddress = dstAddress;
        this.protocol = protocol;
        this.exporterAddress = exporterAddress;
    }

    private ClassificationRequest(final Builder builder) {
        this.location = builder.location;
        this.protocol = builder.protocol;
        this.dstPort = builder.dstPort;
        this.dstAddress = builder.dstAddress;
        this.srcPort = builder.srcPort;
        this.srcAddress = builder.srcAddress;
        this.exporterAddress = builder.exporterAddress;
    }

    public ClassificationRequest() {

    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setProtocol(Integer protocol) {
        this.protocol = protocol;
    }

    public Integer getProtocol() {
        return protocol;
    }

    public void setDstPort(final Integer dstPort) {
        this.dstPort = dstPort;
    }

    public Integer getDstPort() {
        return dstPort;
    }

    public void setDstAddress(String dstAddress) {
        this.dstAddress = IpAddr.of(dstAddress);
    }

    public void setDstAddress(IpAddr dstAddress) {
        this.dstAddress = dstAddress;
    }

    public IpAddr getDstAddress() {
        return dstAddress;
    }

    public Integer getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(final Integer srcPort) {
        this.srcPort = srcPort;
    }

    public IpAddr getSrcAddress() {
        return srcAddress;
    }

    public void setSrcAddress(String srcAddress) {
        this.srcAddress = IpAddr.of(srcAddress);
    }

    public void setSrcAddress(IpAddr srcAddress) {
        this.srcAddress = srcAddress;
    }

    public IpAddr getExporterAddress() {
        return exporterAddress;
    }

    public void setExporterAddress(IpAddr exporterAddress) {
        this.exporterAddress = exporterAddress;
    }

    public void setExporterAddress(String exporterAddress) {
        this.setExporterAddress(IpAddr.of(exporterAddress));
    }

    public boolean isClassifiable() {
        return this.srcPort != null && this.dstPort != null && this.protocol != null;
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

    @Override
    public String toString() {
        return "ClassificationRequest{" +
               "location='" + location + '\'' +
               ", protocol=" + protocol +
               ", dstPort=" + dstPort +
               ", dstAddress='" + dstAddress + '\'' +
               ", srcPort=" + srcPort +
               ", srcAddress='" + srcAddress + '\'' +
               ", exporterAddress='" + exporterAddress + '\'' +
               '}';
    }

    public static class Builder {
        private String location;
        private int protocol;
        private Integer dstPort;
        private IpAddr dstAddress;
        private Integer srcPort;
        private IpAddr srcAddress;
        private IpAddr exporterAddress;

        private Builder() {}

        public Builder withLocation(final String location) {
            this.location = location;
            return this;
        }

        public Builder withProtocol(final Integer protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder withDstPort(final Integer dstPort) {
            this.dstPort = dstPort;
            return this;
        }

        public Builder withDstAddress(final IpAddr dstAddress) {
            this.dstAddress = dstAddress;
            return this;
        }

        public Builder withDstAddress(final String dstAddress) {
            return this.withDstAddress(IpAddr.of(dstAddress));
        }

        public Builder withSrcPort(final Integer srcPort) {
            this.srcPort = srcPort;
            return this;
        }

        public Builder withSrcAddress(final IpAddr srcAddress) {
            this.srcAddress = srcAddress;
            return this;
        }

        public Builder withSrcAddress(final String srcAddress) {
            return this.withSrcAddress(IpAddr.of(srcAddress));
        }

        public Builder withExporterAddress(final IpAddr exporterAddress) {
            this.exporterAddress = exporterAddress;
            return this;
        }

        public Builder withExporterAddress(final String exporterAddress) {
            return this.withExporterAddress(IpAddr.of(exporterAddress));
        }

        public ClassificationRequest build() {
            return new ClassificationRequest(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
