/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.flows.classification;

import java.util.Objects;

import org.opennms.netmgt.flows.classification.persistence.api.Protocol;

public class ClassificationRequest {

    private String location;
    private Protocol protocol;
    private Integer dstPort;
    private IpAddr dstAddress;
    private Integer srcPort;
    private IpAddr srcAddress;
    private String exporterAddress;

    public ClassificationRequest(String location, int srcPort, IpAddr srcAddress, int dstPort, IpAddr dstAddress, Protocol protocol) {
        this.location = location;
        this.srcPort = srcPort;
        this.srcAddress = srcAddress;
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

    public String getExporterAddress() {
        return exporterAddress;
    }

    public void setExporterAddress(String exporterAddress) {
        this.exporterAddress = exporterAddress;
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
}
