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
package org.opennms.netmgt.model.resource;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;

@XmlRootElement(name = "resource")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResourceDTO {

    @XmlAttribute(name = "id")
    private String m_id;

    @XmlAttribute(name = "label")
    private String m_label;

    @XmlAttribute(name = "name")
    private String m_name;

    @XmlAttribute(name = "link")
    private String m_link;

    @XmlAttribute(name="typeLabel")
    private String m_typeLabel;

    @XmlAttribute(name = "parentId")
    private String m_parentId;

    @XmlElement(name="children")
    private ResourceDTOCollection m_children;

    @XmlElementWrapper(name="stringPropertyAttributes")
    private Map<String, String> m_stringPropertyAttributes;

    @XmlElementWrapper(name="externalValueAttributes")
    private Map<String, String> m_externalValueAttributes;

    @XmlElementWrapper(name="rrdGraphAttributes")
    private Map<String, RrdGraphAttribute> m_rrdGraphAttributes;

    @XmlElementWrapper(name="graphNames")
    @XmlElement(name="graphName")
    private List<String> m_graphNames;

    @XmlTransient
    private OnmsResource m_resource;

    public String getId() {
        return m_id;
    }

    public void setId(final String id) {
        this.m_id = id;
    }

    public String getLabel() {
        return m_label;
    }

    public void setLabel(final String label) {
        this.m_label = label;
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        this.m_name = name;
    }

    public String getLink() {
        return m_link;
    }

    public void setLink(final String link) {
        this.m_link = link;
    }

    public String getTypeLabel() {
        return m_typeLabel;
    }

    public void setTypeLabel(final String typeLabel) {
        this.m_typeLabel = typeLabel;
    }

    public String getParentId() {
        return m_parentId;
    }

    public void setParentId(final String parentId) {
        this.m_parentId = parentId;
    }

    public ResourceDTOCollection getChildren() {
        return m_children;
    }

    public void setChildren(final ResourceDTOCollection children) {
        this.m_children = children;
    }

    public Map<String, String> getStringPropertyAttributes() {
        return m_stringPropertyAttributes;
    }

    public void setStringPropertyAttributes(final Map<String, String> stringPropertyAttributes) {
        this.m_stringPropertyAttributes = stringPropertyAttributes;
    }

    public Map<String, String> getExternalValueAttributes() {
        return m_externalValueAttributes;
    }

    public void setExternalValueAttributes(final Map<String, String> externalValueAttributes) {
        this.m_externalValueAttributes = externalValueAttributes;
    }

    public Map<String, RrdGraphAttribute> getRrdGraphAttributes() {
        return m_rrdGraphAttributes;
    }

    public void setGraphNames(final List<String> graphNames) {
        m_graphNames = graphNames;
    }

    public void setRrdGraphAttributes(final Map<String, RrdGraphAttribute> rrdGraphAttributes) {
        this.m_rrdGraphAttributes = rrdGraphAttributes;
    }

    public void setResource(OnmsResource resource) {
        m_resource = resource;
    }

    public OnmsResource getResource() {
        return m_resource;
    }

    public static ResourceDTO fromResource(final OnmsResource resource, final int depth) {
        final ResourceDTO dto = new ResourceDTO();
        dto.setId(resource.getId().toString());
        dto.setLabel(resource.getLabel());
        dto.setName(resource.getName());
        dto.setLink(resource.getLink());
        dto.setTypeLabel(resource.getResourceType().getLabel());
        dto.setParentId(resource.getParent() == null ? null : resource.getParent().getId().toString());
        dto.setStringPropertyAttributes(resource.getStringPropertyAttributes());
        dto.setExternalValueAttributes(resource.getExternalValueAttributes());
        dto.setRrdGraphAttributes(resource.getRrdGraphAttributes());
        dto.setResource(resource);

        if (depth == 0) {
            dto.setChildren(null);
        } else {
            List<ResourceDTO> children = new LinkedList<>();
            for (final OnmsResource child : resource.getChildResources()) {
                children.add(ResourceDTO.fromResource(child, depth - 1));
            }
            dto.setChildren(new ResourceDTOCollection(children));
        }

        return dto;
    }
}
