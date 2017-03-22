/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.kscReports;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Class Report.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "Report")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("ksc-performance-reports.xsd")
public class Report implements java.io.Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "id")
    private Integer m_id;

    @XmlAttribute(name = "title", required = true)
    private String m_title;

    @XmlAttribute(name = "show_timespan_button")
    private Boolean m_showTimespanButton;

    @XmlAttribute(name = "show_graphtype_button")
    private Boolean m_showGraphtypeButton;

    @XmlAttribute(name = "graphs_per_line")
    private Integer m_graphsPerLine;

    @XmlElement(name = "Graph")
    private List<Graph> m_graphs = new ArrayList<>();

    public Optional<Integer> getId() {
        return Optional.ofNullable(m_id);
    }

    public void setId(final Integer id) {
        m_id = id;
    }

    public String getTitle() {
        return m_title;
    }

    public void setTitle(final String title) {
        m_title = ConfigUtils.assertNotEmpty(title, "title");
    }

    public Optional<Boolean> getShowTimespanButton() {
        return Optional.ofNullable(m_showTimespanButton);
    }

    public void setShowTimespanButton(final Boolean showTimespanButton) {
        m_showTimespanButton = showTimespanButton;
    }

    public Optional<Boolean> getShowGraphtypeButton() {
        return Optional.ofNullable(m_showGraphtypeButton);
    }

    public void setShowGraphtypeButton(final Boolean showGraphtypeButton) {
        m_showGraphtypeButton = showGraphtypeButton;
    }

    public List<Graph> getGraphs() {
        return m_graphs;
    }

    public void setGraphs(final List<Graph> graphs) {
        if (graphs == m_graphs) return;
        m_graphs.clear();
        if (graphs != null) m_graphs.addAll(graphs);
    }

    public void addGraph(final Graph graph) {
        m_graphs.add(graph);
    }

    public void addGraph(final int index, final Graph graph) {
        m_graphs.add(index, graph);
    }

    public boolean removeGraph(final Graph graph) {
        return m_graphs.remove(graph);
    }

    public Optional<Integer> getGraphsPerLine() {
        return Optional.ofNullable(m_graphsPerLine);
    }

    public void setGraphsPerLine(final Integer graphsPerLine) {
        m_graphsPerLine = ConfigUtils.assertMinimumInclusive(graphsPerLine, 0, "graphs_per_line");
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_id, 
                            m_title, 
                            m_showTimespanButton, 
                            m_showGraphtypeButton, 
                            m_graphsPerLine, 
                            m_graphs);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Report) {
            final Report that = (Report)obj;
            return Objects.equals(this.m_id, that.m_id)
                    && Objects.equals(this.m_title, that.m_title)
                    && Objects.equals(this.m_showTimespanButton, that.m_showTimespanButton)
                    && Objects.equals(this.m_showGraphtypeButton, that.m_showGraphtypeButton)
                    && Objects.equals(this.m_graphsPerLine, that.m_graphsPerLine)
                    && Objects.equals(this.m_graphs, that.m_graphs);
        }
        return false;
    }

}
