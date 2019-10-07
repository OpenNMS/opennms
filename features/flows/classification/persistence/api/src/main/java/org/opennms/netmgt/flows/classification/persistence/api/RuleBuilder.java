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

package org.opennms.netmgt.flows.classification.persistence.api;

import java.util.Objects;

import com.google.common.base.Strings;

public class RuleBuilder {

    private Rule rule = new Rule();

    public RuleBuilder withName(String name) {
        rule.setName(name);
        return this;
    }

    public RuleBuilder withDstAddress(String dstAddress) {
        rule.setDstAddress(dstAddress);
        return this;
    }

    public RuleBuilder withDstPort(String dstPort) {
        rule.setDstPort(dstPort);
        return this;
    }

    public RuleBuilder withDstPort(int dstPort) {
        rule.setDstPort("" + dstPort);
        return this;
    }


    public RuleBuilder withSrcAddress(String srcAddress) {
        rule.setSrcAddress(srcAddress);
        return this;
    }

    public RuleBuilder withSrcPort(String srcPort) {
        rule.setSrcPort(srcPort);
        return this;
    }

    public RuleBuilder withSrcPort(int srcPort) {
        rule.setSrcPort("" + srcPort);
        return this;
    }

    public RuleBuilder withProtocol(String protocol) {
        rule.setProtocol(protocol);
        return this;
    }

    public RuleBuilder withProtocol(Protocol protocol) {
        rule.setProtocol(protocol.getKeyword());
        return this;
    }

    public RuleBuilder withOmnidirectional(boolean omnidirectional) {
        rule.setOmnidirectional(omnidirectional);
        return this;
    }

    public RuleBuilder withExporterFilter(String exporterFilter) {
        rule.setExporterFilter(exporterFilter);
        return this;
    }

    public RuleBuilder withGroup(Group group) {
        group.addRule(rule);
        return this;
    }

    public RuleBuilder fromRule(Rule rule) {
        Objects.requireNonNull(rule);
        withName(rule.getName());
        withSrcAddress(rule.getSrcAddress());
        withSrcPort(rule.getSrcPort());
        withDstAddress(rule.getDstAddress());
        withDstPort(rule.getDstPort());
        withProtocol(rule.getProtocol());
        withOmnidirectional(rule.isOmnidirectional());
        withExporterFilter(rule.getExporterFilter());
        if (rule.getGroup() != null) {
            withGroup(rule.getGroup());
        }
        return this;
    }

    public RuleBuilder withPosition(int position) {
        rule.setPosition(position);
        return this;
    }

    public Rule build() {
        if (Strings.isNullOrEmpty(rule.getName())) {
            throw new IllegalStateException("Cannot build rule. Field 'name' must not be null or empty.");
        }
        return rule;
    }

}
