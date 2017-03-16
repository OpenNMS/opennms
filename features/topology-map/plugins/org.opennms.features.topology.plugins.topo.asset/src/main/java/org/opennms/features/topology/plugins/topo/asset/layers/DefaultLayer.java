/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
