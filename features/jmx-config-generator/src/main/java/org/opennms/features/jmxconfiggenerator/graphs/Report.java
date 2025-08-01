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
    private List<Graph> graphs = new ArrayList<>();

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
