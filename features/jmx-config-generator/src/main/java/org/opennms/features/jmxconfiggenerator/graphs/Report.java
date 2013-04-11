/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.jmxconfiggenerator.graphs;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class Report {

    private String id;
    private String name;
    private String title;
    private String verticalLabel;
    private List<Graph> graphs = new ArrayList<Graph>();

    public Report(String id, String name, String title, String verticalLabel) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.verticalLabel = verticalLabel;
    }

    public String getGraphResources() {
        String result = "";
        for (Graph graph : graphs) {
            result = result.concat(graph.getResourceName() + ", ");
        }
        if (result.length() > 2) {
            result = result.substring(0, (result.length() - 2));
        }
        return result;
    }

    public List<Graph> getGraphs() {
        return graphs;
    }

    public void setGraphs(List<Graph> graphs) {
        this.graphs = graphs;
    }

    public void addGraph(Graph graph) {
        this.graphs.add(graph);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVerticalLabel() {
        return verticalLabel;
    }

    public void setVerticalLabel(String verticalLabel) {
        this.verticalLabel = verticalLabel;
    }

    @Override
    public String toString() {
        return "Report{" + "id=" + id + ", name=" + name + ", title=" + title + ", verticalLabel=" + verticalLabel + ", graphs=" + graphs + '}';
    }
}
