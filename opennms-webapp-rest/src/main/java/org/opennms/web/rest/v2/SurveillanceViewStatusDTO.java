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
package org.opennms.web.rest.v2;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The computed grid of a surveillance view: row/column labels in definition
 * order plus one cell per row/column pair, indexed {@code cells[row][column]}.
 */
public class SurveillanceViewStatusDTO {

    /** One grid cell: nodes with at least one down service, out of all nodes. */
    public static class Cell {

        @JsonProperty("down")
        private Integer down;

        @JsonProperty("total")
        private Integer total;

        @JsonProperty("status")
        private String status;

        public Cell() {
        }

        public Cell(Integer down, Integer total, String status) {
            this.down = down;
            this.total = total;
            this.status = status;
        }

        public Integer getDown() {
            return down;
        }

        public void setDown(Integer down) {
            this.down = down;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    @JsonProperty("viewId")
    private String viewId;

    @JsonProperty("viewName")
    private String viewName;

    @JsonProperty("refreshSeconds")
    private Integer refreshSeconds;

    @JsonProperty("rows")
    private List<String> rows = new ArrayList<>();

    @JsonProperty("columns")
    private List<String> columns = new ArrayList<>();

    @JsonProperty("cells")
    private List<List<Cell>> cells = new ArrayList<>();

    public String getViewId() {
        return viewId;
    }

    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Integer getRefreshSeconds() {
        return refreshSeconds;
    }

    public void setRefreshSeconds(Integer refreshSeconds) {
        this.refreshSeconds = refreshSeconds;
    }

    public List<String> getRows() {
        return rows;
    }

    public void setRows(List<String> rows) {
        this.rows = rows;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<List<Cell>> getCells() {
        return cells;
    }

    public void setCells(List<List<Cell>> cells) {
        this.cells = cells;
    }
}
