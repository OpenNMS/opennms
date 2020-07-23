/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.graphml.model;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class GraphMLElement {

    protected static final String ID = "id";

    public interface GraphMLElementVisitor<T> {
        T visit(GraphMLGraph graph);
        T visit(GraphMLNode node);
        T visit(GraphMLEdge edge);
        T visit(GraphML graphML);
    }

    private final Map<String, Object> properties = new HashMap<>();

    public String getId() {
        return getProperty(ID);
    }

    public void setId(String id) {
        setProperty(ID, Objects.requireNonNull(id));
    }

    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public <T> T getProperty(String key, T defaultValue) {
        return (T) properties.getOrDefault(key, defaultValue);
    }

    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public HashMap<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    public abstract <T> T accept(GraphMLElementVisitor<T> visitor);

    @Override
    public int hashCode() {
        return properties.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof GraphMLElement) {
            return Objects.equals(properties, ((GraphMLElement)obj).properties);
        }
        return false;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(getClass())
                .add("id", getId()).toString();
    }
}
