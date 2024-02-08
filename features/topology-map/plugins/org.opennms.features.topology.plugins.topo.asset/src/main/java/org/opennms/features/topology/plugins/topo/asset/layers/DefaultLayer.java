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

import org.opennms.features.topology.api.support.FocusStrategy;
import org.opennms.features.topology.plugins.topo.asset.layers.decorator.NodeItemNodeDecorator;
import org.opennms.netmgt.model.OnmsNode;

/**
 * Simply POJO for a layer.
 *
 * @author mvrueden
 */
public class DefaultLayer implements Layer {

    private String id;
    private String label;
    private String namespace;
    private String description;
    private int szl = 0;
    private boolean vertexStatusProvider = false;
    private FocusStrategy focusStrategy = FocusStrategy.ALL;
    private ItemProvider<OnmsNode> itemProvider;
    private IdGenerator idGenerator;

    protected void setId(String id) {
        this.id = id;
    }

    protected void setLabel(String label) {
        this.label = label;
    }

    protected void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setItemProvider(ItemProvider<OnmsNode> itemProvider) {
        this.itemProvider = itemProvider;
    }

    protected void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    protected void setSzl(int szl) {
        this.szl = szl;
    }

    protected void setVertexStatusProvider(boolean vertexStatusProvider) {
        this.vertexStatusProvider = vertexStatusProvider;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean hasVertexStatusProvider() {
        return vertexStatusProvider;
    }

    @Override
    public int getSemanticZoomLevel() {
        return szl;
    }

    @Override
    public FocusStrategy getFocusStrategy() {
        return focusStrategy;
    }

    @Override
    public NodeDecorator getNodeDecorator() {
        return new NodeItemNodeDecorator();
    }

    @Override
    public ItemProvider getItemProvider() {
        return itemProvider;
    }

    @Override
    public IdGenerator getIdGenerator() {
        return idGenerator;
    }
}
