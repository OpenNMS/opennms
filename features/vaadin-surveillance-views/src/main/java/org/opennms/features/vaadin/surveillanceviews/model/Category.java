package org.opennms.features.vaadin.surveillanceviews.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Category {
    @XmlAttribute(name = "name", required = true)
    private java.lang.String m_name;

    public String getName() {
        return m_name;
    }
}
