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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.xml.NumberAdapter;
import org.opennms.netmgt.collection.support.builder.NumericAttribute;

@XmlRootElement(name = "numeric-attribute")
@XmlAccessorType(XmlAccessType.NONE)
public class NumericAttributeDTO extends AttributeDTO {

    @XmlAttribute(name="value")
    @XmlJavaTypeAdapter(NumberAdapter.class)
    private Number value;

    public NumericAttributeDTO() { }

    public NumericAttributeDTO(NumericAttribute attribute) {
        super(attribute);
        this.value = attribute.getNumericValue();
    }

    public NumericAttribute toAttribute() {
        return new NumericAttribute(getGroup(), getName(), value, getType(), getIdentifier());
    }

}
