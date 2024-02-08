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
package org.opennms.netmgt.endpoints.grafana.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public class Panel implements PanelContainer {

    private Integer id;
    private String title;
    private String type;
    private String datasource;
    private String description;
    private List<Panel> panels = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public List<Panel> getPanels() {
        return panels;
    }

    public void setPanels(List<Panel> panels) {
        this.panels = panels;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Panel)) return false;
        if (!super.equals(object)) return false;
        Panel panel = (Panel) object;
        return Objects.equals(id, panel.id) &&
                Objects.equals(title, panel.title) &&
                Objects.equals(type, panel.type) &&
                Objects.equals(datasource, panel.datasource) &&
                Objects.equals(description, panel.description) &&
                Objects.equals(panels, panel.panels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, type, datasource, description, panels);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("title", title)
                .add("type", type)
                .add("datasource", datasource)
                .add("description", description)
                .add("panels", panels)
                .toString();
    }
}
