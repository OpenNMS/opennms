package org.opennms.features.vaadin.surveillanceviews.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.LinkedList;
import java.util.List;

public class ColumnDef {

    @XmlAttribute(name = "label", required = true)
    private java.lang.String m_label = "default";

    public String getLabel() {
        return m_label;
    }

    @XmlAttribute(name = "report-category", required = false)
    private java.lang.String m_reportCategory;

    public String getReportCetegory() {
        return m_reportCategory;
    }

    private List<Category> m_categories = new LinkedList<Category>();

    @XmlElement(name = "category")
    public List<Category> getCategories() {
        return m_categories;
    }
}
