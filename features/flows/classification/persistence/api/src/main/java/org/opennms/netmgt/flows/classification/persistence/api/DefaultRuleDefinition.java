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
package org.opennms.netmgt.flows.classification.persistence.api;

import java.util.Objects;

public class DefaultRuleDefinition implements RuleDefinition {

    private String name;
    private String dstAddress;
    private String dstPort;
    private String srcPort;
    private String srcAddress;
    private String protocol;
    private String exporterFilter;
    private int groupPosition;
    private int position;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDstAddress() {
        return dstAddress;
    }

    @Override
    public String getDstPort() {
        return dstPort;
    }

    @Override
    public String getSrcPort() {
        return srcPort;
    }

    @Override
    public String getSrcAddress() {
        return srcAddress;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getExporterFilter() {
        return exporterFilter;
    }

    @Override
    public int getGroupPosition() {
        return groupPosition;
    }

    @Override
    public int getPosition() {
        return position;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
    }

    public void setDstPort(String dstPort) {
        this.dstPort = dstPort;
    }

    public void setSrcPort(String srcPort) {
        this.srcPort = srcPort;
    }

    public void setSrcAddress(String srcAddress) {
        this.srcAddress = srcAddress;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setExporterFilter(String exporterFilter) {
        this.exporterFilter = exporterFilter;
    }

    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultRuleDefinition that = (DefaultRuleDefinition) o;
        return Objects.equals(groupPosition, that.groupPosition)
                && Objects.equals(name, that.name)
                && Objects.equals(dstAddress, that.dstAddress)
                && Objects.equals(dstPort, that.dstPort)
                && Objects.equals(srcPort, that.srcPort)
                && Objects.equals(srcAddress, that.srcAddress)
                && Objects.equals(protocol, that.protocol)
                && Objects.equals(exporterFilter, that.exporterFilter)
                && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dstAddress, dstPort, srcPort, srcAddress, protocol, exporterFilter, groupPosition, position);
    }

}
