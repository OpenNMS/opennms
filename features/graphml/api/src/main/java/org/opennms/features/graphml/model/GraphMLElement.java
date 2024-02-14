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
        return com.google.common.base.MoreObjects.toStringHelper(getClass())
                .add("id", getId()).toString();
    }
}
