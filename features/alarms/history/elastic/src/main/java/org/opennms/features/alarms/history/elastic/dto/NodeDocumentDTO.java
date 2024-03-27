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
package org.opennms.features.alarms.history.elastic.dto;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.gson.annotations.SerializedName;

public class NodeDocumentDTO {

    @SerializedName("categories")
    private List<String> categories;

    @SerializedName("foreign_id")
    private String foreignId;

    @SerializedName("foreign_source")
    private String foreignSource;
    
    @SerializedName("id")
    private Integer id;

    @SerializedName("label")
    private String label;

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories == null ? null : ImmutableList.copyOf(categories);
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "NodeDocumentDTO{" +
                "categories=" + categories +
                ", foreignId='" + foreignId + '\'' +
                ", foreignSource='" + foreignSource + '\'' +
                ", id=" + id +
                ", label='" + label + '\'' +
                '}';
    }
}
