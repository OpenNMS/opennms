package org.opennms.features.vaadin.surveillanceviews.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "surveillance-view-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("all")
public class SurveillanceViewConfiguration implements java.io.Serializable {

    @XmlAttribute(name = "default-view")
    private java.lang.String m_defaultView = "default";

    private List<View> m_views = new LinkedList<View>();

    @XmlElement(name = "view")
    @XmlElementWrapper(name = "views")
    public List<View> getViews() {
        return m_views;
    }

}
