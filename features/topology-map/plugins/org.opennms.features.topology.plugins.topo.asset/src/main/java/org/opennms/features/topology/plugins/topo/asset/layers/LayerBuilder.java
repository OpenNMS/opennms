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
package org.opennms.features.topology.plugins.topo.asset.layers;

/**
 * Allows building a generic layer.
 *
 * @author mvrueden
 */
public class LayerBuilder {

    private DefaultLayer layer = new DefaultLayer();

    public LayerBuilder withId(String id) {
        layer.setId(id);
        return this;
    }

    public LayerBuilder withNamespace(String namespace) {
        layer.setNamespace(namespace);
        return this;
    }

    public LayerBuilder withLabel(String label) {
        layer.setLabel(label);
        return this;
    }

    public LayerBuilder withSemanticZoomLevel(int szl) {
        layer.setSzl(szl);
        return this;
    }

    public LayerBuilder withDescription(String description) {
        layer.setDescription(description);
        return this;
    }

    public LayerBuilder withItemProvider(ItemProvider itemProvider) {
        layer.setItemProvider(itemProvider);
        return this;
    }

    public LayerBuilder withIdGenerator(IdGenerator idGenerator) {
        layer.setIdGenerator(idGenerator);
        return this;
    }

    public LayerBuilder withVertexStatusProvider(boolean vertexStatusProvider) {
        layer.setVertexStatusProvider(vertexStatusProvider);
        return this;
    }

    public Layer build() {
        return layer;
    }
}
