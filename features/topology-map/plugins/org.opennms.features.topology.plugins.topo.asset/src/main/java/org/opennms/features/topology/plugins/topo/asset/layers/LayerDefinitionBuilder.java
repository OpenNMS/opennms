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

/**
 * Allows building a generic layer.
 *
 * @author mvrueden
 */
public class LayerDefinitionBuilder {

    private DefaultLayer definition = new DefaultLayer();

    public LayerDefinitionBuilder withId(String id) {
        definition.setId(id);
        return this;
    }

    public LayerDefinitionBuilder withNamespace(String namespace) {
        definition.setNamespace(namespace);
        return this;
    }

    public LayerDefinitionBuilder withLabel(String label) {
        definition.setLabel(label);
        return this;
    }

    public LayerDefinitionBuilder withDescription(String description) {
        definition.setDescription(description);
        return this;
    }

    public LayerDefinitionBuilder withItemProvider(ItemProvider itemProvider) {
        definition.setItemProvider(itemProvider);
        return this;
    }

    public LayerDefinitionBuilder withIdGenerator(IdGenerator idGenerator) {
        definition.setIdGenerator(idGenerator);
        return this;
    }

    public Layer build() {
        return definition;
    }
}
