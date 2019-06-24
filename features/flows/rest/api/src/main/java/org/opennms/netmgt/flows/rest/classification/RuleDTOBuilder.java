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

public class RuleDTOBuilder {
    private final RuleDTO ruleDTO = new RuleDTO();

    public RuleDTOBuilder withName(String name) {
        this.ruleDTO.setName(name);
        return this;
    }

    public RuleDTOBuilder withProtocol(String protocol) {
        this.ruleDTO.setProtocol(protocol);
        return this;
    }

    public RuleDTOBuilder withDstPort(String dstPort) {
        this.ruleDTO.setDstPort(dstPort);
        return this;
    }

    public RuleDTOBuilder withDstAddress(String dstAddress) {
        this.ruleDTO.setDstAddress(dstAddress);
        return this;
    }

    public RuleDTOBuilder withSrcPort(String srcPort) {
        this.ruleDTO.setSrcPort(srcPort);
        return this;
    }

    public RuleDTOBuilder withSrcAddress(String srcAddress) {
        this.ruleDTO.setSrcAddress(srcAddress);
        return this;
    }

    public RuleDTOBuilder withExporterFilter(String exporterFilter) {
        this.ruleDTO.setExporterFilter(exporterFilter);
        return this;
    }

    public RuleDTOBuilder withOmnidirectional(boolean omnidirectional) {
        this.ruleDTO.setOmnidirectional(omnidirectional);
        return this;
    }

    public RuleDTO build() {
        return ruleDTO;
    }
}
