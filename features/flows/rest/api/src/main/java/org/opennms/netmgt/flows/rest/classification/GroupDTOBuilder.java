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

public class GroupDTOBuilder {
    private final GroupDTO groupDTO = new GroupDTO();

    public GroupDTOBuilder withId(Integer id){
        this.groupDTO.setId(id);
        return this;
    }

    public GroupDTOBuilder withPosition(Integer position){
        this.groupDTO.setPosition(position);
        return this;
    }

    public GroupDTOBuilder withName(String name){
        this.groupDTO.setName(name);
        return this;
    }

    public GroupDTOBuilder withDescription(String description){
        this.groupDTO.setDescription(description);
        return this;
    }

    public GroupDTOBuilder withEnabled(Boolean enabled){
        this.groupDTO.setEnabled(enabled);
        return this;
    }

    public GroupDTOBuilder withReadOnly(Boolean readOnly){
        this.groupDTO.setReadOnly(readOnly);
        return this;
    }

    public GroupDTOBuilder withRuleCount(Integer ruleCount){
        this.groupDTO.setRuleCount(ruleCount);
        return this;
    }

    public GroupDTO build() {
        return groupDTO;
    }
}
