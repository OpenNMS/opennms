/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
