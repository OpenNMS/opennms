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
