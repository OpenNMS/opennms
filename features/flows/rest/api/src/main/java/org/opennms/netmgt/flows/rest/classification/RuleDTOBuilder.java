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

    public RuleDTOBuilder withPosition(int position) {
        this.ruleDTO.setPosition(position);
        return this;
    }

    public RuleDTOBuilder withGroup(GroupDTO group) {
        this.ruleDTO.setGroup(group);
        return this;
    }

    public RuleDTO build() {
        return ruleDTO;
    }
}
