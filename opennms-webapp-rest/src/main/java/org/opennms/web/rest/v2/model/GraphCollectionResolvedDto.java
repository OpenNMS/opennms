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
package org.opennms.web.rest.v2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A graph collection prepared for the view page. type is "custom" for a saved
 * collection (id is then set) or "node", "nodeSource" or "domain" for the
 * collections built on the fly from a resource's SNMP interfaces. graphsPerLine
 * has the system default applied, timespan and graphtype echo the overrides
 * that were applied (null when none), and graphTypes is the union of the
 * prefabricated graphs available to the resolved resources, which feeds the
 * graph type override selector.
 */
public class GraphCollectionResolvedDto {
    private Integer id;
    private String type;
    private String title;
    private boolean showTimespanButton;
    private boolean showGraphtypeButton;
    private int graphsPerLine;
    private String timespan;
    private String graphtype;
    private List<String> graphTypes = new ArrayList<>();
    private List<GraphCollectionResolvedGraphDto> graphs = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public boolean isShowTimespanButton() {
        return showTimespanButton;
    }

    public void setShowTimespanButton(final boolean showTimespanButton) {
        this.showTimespanButton = showTimespanButton;
    }

    public boolean isShowGraphtypeButton() {
        return showGraphtypeButton;
    }

    public void setShowGraphtypeButton(final boolean showGraphtypeButton) {
        this.showGraphtypeButton = showGraphtypeButton;
    }

    public int getGraphsPerLine() {
        return graphsPerLine;
    }

    public void setGraphsPerLine(final int graphsPerLine) {
        this.graphsPerLine = graphsPerLine;
    }

    public String getTimespan() {
        return timespan;
    }

    public void setTimespan(final String timespan) {
        this.timespan = timespan;
    }

    public String getGraphtype() {
        return graphtype;
    }

    public void setGraphtype(final String graphtype) {
        this.graphtype = graphtype;
    }

    public List<String> getGraphTypes() {
        return graphTypes;
    }

    public void setGraphTypes(final List<String> graphTypes) {
        this.graphTypes = graphTypes == null ? new ArrayList<>() : graphTypes;
    }

    public List<GraphCollectionResolvedGraphDto> getGraphs() {
        return graphs;
    }

    public void setGraphs(final List<GraphCollectionResolvedGraphDto> graphs) {
        this.graphs = graphs == null ? new ArrayList<>() : graphs;
    }
}
