package org.opennms.features.vaadin.surveillanceviews.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement
public class View {

    private List<RowDef> m_rows = new LinkedList<RowDef>();

    @XmlElement(name = "row-def")
    @XmlElementWrapper(name = "rows")
    public List<RowDef> getRows() {
        return m_rows;
    }

    private List<ColumnDef> m_columns = new LinkedList<ColumnDef>();

    @XmlElement(name = "column-def")
    @XmlElementWrapper(name = "columns")
    public List<ColumnDef> getColumns() {
        return m_columns;
    }

    @XmlAttribute(name = "name", required = true)
    private java.lang.String m_name = "default";

    public String getName() {
        return m_name;
    }

    @XmlAttribute(name = "refresh-seconds", required = false)
    private java.lang.Integer m_refreshSeconds = 300;

    public int getRefreshSeconds() {
        return m_refreshSeconds;
    }
}
