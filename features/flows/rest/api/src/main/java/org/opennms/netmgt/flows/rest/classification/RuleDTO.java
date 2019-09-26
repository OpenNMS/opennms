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

package org.opennms.netmgt.flows.rest.classification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class RuleDTO {
    private Integer id;
    private String name;
    private String dstAddress;
    private String dstPort;
    private String srcAddress;
    private String srcPort;
    private String exporterFilter;
    private boolean omnidirectional;

    private GroupDTO group;
    private Integer position;
    private List<String> protocols = new ArrayList<>();

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDstAddress() {
        return dstAddress;
    }

    public void setDstAddress(String dstAddress) {
        this.dstAddress = dstAddress;
    }

    public String getDstPort() {
        return dstPort;
    }

    public void setDstPort(String dstPort) {
        this.dstPort = dstPort;
    }

    public String getSrcAddress() {
        return srcAddress;
    }

    public void setSrcAddress(String srcAddress) {
        this.srcAddress = srcAddress;
    }

    public String getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(String srcPort) {
        this.srcPort = srcPort;
    }

    public void setProtocol(String protocol) {
        if (protocol == null) {
            setProtocols(Lists.newArrayList());
        } else {
            setProtocols(Arrays.stream(protocol.split(","))
                    .map(segment -> segment.trim())
                    .filter(segment -> segment != null && segment.length() > 0)
                    .sorted()
                    .collect(Collectors.toList()));
        }
    }

    public List<String> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<String> protocols) {
        this.protocols = protocols;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getPosition() {
        return position;
    }

    public String getExporterFilter() {
        return exporterFilter;
    }

    public void setExporterFilter(String exporterFilter) {
        this.exporterFilter = exporterFilter;
    }

    public boolean isOmnidirectional() {
        return this.omnidirectional;
    }

    public void setOmnidirectional(final boolean omnidirectional) {
        this.omnidirectional = omnidirectional;
    }

    public void setGroup(GroupDTO group) {
        this.group = group;
    }

    public GroupDTO getGroup() {
        return group;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final RuleDTO that = (RuleDTO) o;
        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(dstAddress, that.dstAddress)
                && Objects.equals(dstPort, that.dstPort)
                && Objects.equals(srcAddress, that.srcAddress)
                && Objects.equals(srcPort, that.srcPort)
                && Objects.equals(protocols, that.protocols)
                && Objects.equals(exporterFilter, that.exporterFilter)
                && Objects.equals(omnidirectional, that.omnidirectional)
                && Objects.equals(group, that.group)
                && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, dstAddress, dstPort, srcAddress, srcPort, protocols, exporterFilter, omnidirectional, group, position);
    }
}
