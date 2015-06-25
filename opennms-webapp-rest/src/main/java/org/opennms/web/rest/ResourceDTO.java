package org.opennms.web.rest;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;

import com.google.common.collect.Lists;

@XmlRootElement(name = "resource")
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceDTO {

    @XmlAttribute(name = "id")
    private final String m_id;

    @XmlAttribute(name = "label")
    private final String m_label;

    @XmlAttribute(name = "name")
    private final String m_name;

    @XmlAttribute(name = "link")
    private final String m_link;

    @XmlAttribute(name="typeLabel")
    private final String m_typeLabel;

    @XmlAttribute(name = "parentId")
    private final String m_parentId;

    @XmlElement(name="children")
    private final ResourceDTOCollection m_children;

    @XmlElementWrapper(name="stringPropertyAttributes")
    private final Map<String, String> m_stringPropertyAttributes;

    @XmlElementWrapper(name="externalValueAttributes")
    private final Map<String, String> m_externalValueAttributes;

    @XmlElementWrapper(name="rrdGraphAttributes")
    private final Map<String, RrdGraphAttribute> m_rrdGraphAttributes;

    @XmlElementWrapper(name="graphNames")
    @XmlElement(name="graphName")
    private List<String> m_graphNames;

    protected ResourceDTO() {
        throw new UnsupportedOperationException("No-arg constructor for JAXB.");
    }

    public ResourceDTO(final OnmsResource resource) {
        this(resource, -1);
    }

    public ResourceDTO(final OnmsResource resource, final int depth) {
        m_id = resource.getId();
        m_label = resource.getLabel();
        m_name = resource.getName();
        m_link = resource.getLink();
        m_typeLabel = resource.getResourceType().getLabel();
        m_parentId = resource.getParent() == null ? null : resource.getParent().getId();
        m_stringPropertyAttributes = resource.getStringPropertyAttributes();
        m_externalValueAttributes = resource.getExternalValueAttributes();
        m_rrdGraphAttributes = resource.getRrdGraphAttributes();

        if (depth == 0) {
            m_children = null;
        } else {
            List<ResourceDTO> children = Lists.newLinkedList();
            for (final OnmsResource child : resource.getChildResources()) {
                children.add(new ResourceDTO(child, depth-1));
            }
            m_children = new ResourceDTOCollection(children);
        }
    }

    public String getId() {
        return m_id;
    }

    public String getLabel() {
        return m_label;
    }

    public String getName() {
        return m_name;
    }

    public String getLink() {
        return m_link;
    }

    public String getTypeLabel() {
        return m_typeLabel;
    }

    public String getParentId() {
        return m_parentId;
    }

    public ResourceDTOCollection getChildren() {
        return m_children;
    }

    public Map<String, String> getStringPropertyAttributes() {
        return m_stringPropertyAttributes;
    }

    public Map<String, String> getExternalValueAttributes() {
        return m_externalValueAttributes;
    }

    public Map<String, RrdGraphAttribute> getRrdGraphAttributes() {
        return m_rrdGraphAttributes;
    }

    void setGraphNames(final List<String> graphNames) {
        m_graphNames = graphNames;
    }
}