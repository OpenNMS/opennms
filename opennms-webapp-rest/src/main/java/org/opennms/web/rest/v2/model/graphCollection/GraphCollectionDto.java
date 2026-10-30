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
package org.opennms.web.rest.v2.model.graphCollection;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * A graph collection (formerly a KSC report) as exposed by the v2 API: a titled,
 * ordered list of prefabricated graphs plus the layout options of the view page.
 * Field names are camel-cased versions of the attributes in
 * ksc-performance-reports.xml. The id is assigned by the server on create and is
 * ignored in request bodies otherwise.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphCollectionDto {
    private Integer id;
    private String title;
    private Boolean showTimespanButton;
    private Boolean showGraphtypeButton;
    private Integer graphsPerLine;
    private List<GraphCollectionGraphDto> graphs = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Boolean getShowTimespanButton() {
        return showTimespanButton;
    }

    public void setShowTimespanButton(final Boolean showTimespanButton) {
        this.showTimespanButton = showTimespanButton;
    }

    public Boolean getShowGraphtypeButton() {
        return showGraphtypeButton;
    }

    public void setShowGraphtypeButton(final Boolean showGraphtypeButton) {
        this.showGraphtypeButton = showGraphtypeButton;
    }

    public Integer getGraphsPerLine() {
        return graphsPerLine;
    }

    public void setGraphsPerLine(final Integer graphsPerLine) {
        this.graphsPerLine = graphsPerLine;
    }

    public List<GraphCollectionGraphDto> getGraphs() {
        return graphs;
    }

    public void setGraphs(final List<GraphCollectionGraphDto> graphs) {
        this.graphs = graphs == null ? new ArrayList<>() : graphs;
    }
}
