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
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Wire representation of a surveillance view definition. {@code isDefault}
 * is read-only and reflects the catalog's default-view setting; it is set via
 * {@code PUT surveillance/views/default/{id}}, not through this body.
 */
public class SurveillanceViewDTO {

    /** One row or column: a label plus the node category names it aggregates. */
    public static class Def {

        @JsonProperty("label")
        private String label;

        @JsonProperty("categories")
        private List<String> categories = new ArrayList<>();

        @JsonProperty("reportCategory")
        private String reportCategory;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<String> getCategories() {
            return categories;
        }

        public void setCategories(List<String> categories) {
            this.categories = categories == null ? new ArrayList<>() : categories;
        }

        public String getReportCategory() {
            return reportCategory;
        }

        public void setReportCategory(String reportCategory) {
            this.reportCategory = reportCategory;
        }
    }

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("refreshSeconds")
    private Integer refreshSeconds;

    @JsonProperty("isDefault")
    private Boolean isDefault;

    @JsonProperty("rows")
    private List<Def> rows = new ArrayList<>();

    @JsonProperty("columns")
    private List<Def> columns = new ArrayList<>();

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("created")
    private Date created;

    @JsonProperty("lastModified")
    private Date lastModified;

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

    public Integer getRefreshSeconds() {
        return refreshSeconds;
    }

    public void setRefreshSeconds(Integer refreshSeconds) {
        this.refreshSeconds = refreshSeconds;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public List<Def> getRows() {
        return rows;
    }

    public void setRows(List<Def> rows) {
        this.rows = rows == null ? new ArrayList<>() : rows;
    }

    public List<Def> getColumns() {
        return columns;
    }

    public void setColumns(List<Def> columns) {
        this.columns = columns == null ? new ArrayList<>() : columns;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
