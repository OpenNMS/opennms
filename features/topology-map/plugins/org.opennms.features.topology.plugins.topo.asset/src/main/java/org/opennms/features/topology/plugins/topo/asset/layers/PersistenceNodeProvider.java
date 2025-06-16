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
