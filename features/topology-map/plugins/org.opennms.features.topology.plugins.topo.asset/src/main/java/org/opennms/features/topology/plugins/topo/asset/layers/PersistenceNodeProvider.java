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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.plugins.topo.asset.NodeProvider;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceNodeProvider implements NodeProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PersistenceNodeProvider.class);

    private final GenericPersistenceAccessor genericPersistenceAccessor;

    public PersistenceNodeProvider(GenericPersistenceAccessor genericPersistenceAccessor) {
        this.genericPersistenceAccessor = genericPersistenceAccessor;
    }

    @Override
    public List<OnmsNode> getNodes(List<LayerDefinition> definitions) {
        final StringBuilder queryBuilder = new StringBuilder("Select n from OnmsNode n join n.assetRecord assetRecord");
        final List<String> restrictions = definitions.stream().map(LayerDefinition::getRestriction).filter(Objects::nonNull).collect(Collectors.toList());
        if (!restrictions.isEmpty()) {
            queryBuilder.append(" WHERE ");
            queryBuilder.append(restrictions.stream().map(restriction -> "n." + restriction).collect(Collectors.joining(" AND ")));
        }
        final String query = queryBuilder.toString();
        LOG.debug("Query to fetch nodes to build topology from: {}", query);
        return genericPersistenceAccessor.find(query);
    }
}
