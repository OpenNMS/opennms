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
package org.opennms.netmgt.collection.dto;

import javax.xml.bind.annotation.XmlAttribute;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.support.builder.Attribute;

public class AttributeDTO {

    @XmlAttribute(name="group")
    private String group;

    @XmlAttribute(name="name")
    private String name;

    @XmlAttribute(name="type")
    private AttributeType type;

    @XmlAttribute(name="identifier")
    private String identifier;

    public AttributeDTO() { }

    public AttributeDTO(Attribute<?> attribute) {
        group = attribute.getGroup();
        name = attribute.getName();
        type = attribute.getType();
        identifier = attribute.getIdentifier();
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
