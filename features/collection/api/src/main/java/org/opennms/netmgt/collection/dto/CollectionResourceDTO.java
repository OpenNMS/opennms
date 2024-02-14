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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.collection.support.builder.Attribute;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.NumericAttribute;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.collection.support.builder.StringAttribute;

/**
 * 
 * The @XmlJavaTypeAdapter annotation doesn't work properly when using
 * the @XmlElements annotation unless we explicitly declare the types
 * using @XmlElement. These are defined bellow as variables named workaround_*.
 * For further details, see:
 *     https://bugs.eclipse.org/bugs/show_bug.cgi?id=419310
 *     http://stackoverflow.com/questions/19266097/jaxb-moxy-using-xmlelements-with-xmladapter
 * 
 * @author jwhite
 */
@XmlRootElement(name = "collection-resource")
@XmlAccessorType(XmlAccessType.NONE)
public class CollectionResourceDTO {

    @XmlElement
    private final NodeLevelResource workaround_nlr = null;

    @XmlElement
    private final InterfaceLevelResource workaround_ilr = null;

    @XmlElement
    private final DeferredGenericTypeResource workaround_dgtr = null;

    @XmlElement
    private final GenericTypeResource workaround_gtr = null;

    @XmlElement
    private final NumericAttribute workaround_na = null;
    
    @XmlElement
    private final StringAttribute workaround_sa = null;

    @XmlElements({ 
        @XmlElement(name = "node-level-resource", type = NodeLevelResource.class),
        @XmlElement(name = "interface-level-resource", type = InterfaceLevelResource.class),
        @XmlElement(name = "generic-type-resource", type = GenericTypeResource.class),
        @XmlElement(name = "generic-type-resource", type = DeferredGenericTypeResource.class)
    })
    private Resource resource;

    @XmlElements({ 
        @XmlElement(name = "numeric-attribute", type = NumericAttribute.class),
        @XmlElement(name = "string-attribute", type = StringAttribute.class)
    })
    private List<Attribute<?>> attributes = new ArrayList<>();

    public CollectionResourceDTO() { }

    public CollectionResourceDTO(Resource resource, List<Attribute<?>> attributes) {
        this.resource = resource;
        this.attributes = attributes;
    }

    public Resource getResource() {
        // Resolve the resource and store the result.
        // For DeferredGenericTypeResource resources, this is used
        // to lookup the resource definition and construct
        // the appropriate GenericTypeResource
        this.resource = resource != null ? resource.resolve() : null;
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public List<Attribute<?>> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute<?>> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return String.format("CollectionSetAttributesDTO[resource=%s, attributes=%s]",
                resource, attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof CollectionResourceDTO)) {
            return false;
        }
        CollectionResourceDTO other = (CollectionResourceDTO) obj;
        return Objects.equals(this.resource, other.resource)
               && Objects.equals(this.attributes, other.attributes);
    }

}
