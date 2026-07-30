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
package org.opennms.netmgt.graph;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.json.JSONArray;
import org.json.JSONTokener;

import com.google.common.collect.Lists;

@Entity
@Table(name="graph_focus")
public class FocusEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "graphSequence")
    @SequenceGenerator(name = "graphSequence", sequenceName = "graphnxtid", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name="type", nullable = false)
    private String type;

    @Column(name = "selection")
    private String selection;

    public FocusEntity() {

    }

    public FocusEntity(String type) {
        this.type = Objects.requireNonNull(type);
    }

    public FocusEntity(String type, List<String> vertexIds) {
        this(type);
        setSelection(vertexIds);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }


    public void setSelection(List<String> vertexIds) {
        final JSONArray vertexIdArray = new JSONArray(vertexIds);
        setSelection(vertexIdArray.toString());
    }

    public List<String> getVertexIds() {
        final String jsonString = this.selection;
        final JSONTokener tokener = new JSONTokener(jsonString);
        final JSONArray jsonArray = new JSONArray(tokener);
        final List<String> vertices = Lists.newArrayList();
        for (int i=0; i<jsonArray.length(); i++) {
            final String vertexId = jsonArray.getString(i);
            vertices.add(vertexId);
        }
        return vertices;
    }
}
