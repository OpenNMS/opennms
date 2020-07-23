/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
    @SequenceGenerator(name = "graphSequence", sequenceName = "graphnxtid")
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
