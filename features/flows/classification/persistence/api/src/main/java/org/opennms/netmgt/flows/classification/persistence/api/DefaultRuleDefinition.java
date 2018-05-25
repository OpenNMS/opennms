/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
    private int groupPriority;

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
    public int getGroupPriority() {
        return groupPriority;
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

    public void setGroupPriority(int groupPriority) {
        this.groupPriority = groupPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultRuleDefinition that = (DefaultRuleDefinition) o;
        return Objects.equals(groupPriority, that.groupPriority)
                && Objects.equals(name, that.name)
                && Objects.equals(dstAddress, that.dstAddress)
                && Objects.equals(dstPort, that.dstPort)
                && Objects.equals(srcPort, that.srcPort)
                && Objects.equals(srcAddress, that.srcAddress)
                && Objects.equals(protocol, that.protocol)
                && Objects.equals(exporterFilter, that.exporterFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dstAddress, dstPort, srcPort, srcAddress, protocol, exporterFilter, groupPriority);
    }

}
