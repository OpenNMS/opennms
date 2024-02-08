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
package org.opennms.features.apilayer.utils;

import java.util.Objects;
import java.util.Optional;

import org.opennms.integration.api.v1.model.NodeCriteria;
import org.opennms.integration.api.v1.model.immutables.ImmutableNodeCriteria;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class NodeCriteriaLoadingCacheImpl implements NodeCriteriaCache {
    private final LoadingCache<Long, NodeCriteria> nodeIdToCriteriaCache;

    public NodeCriteriaLoadingCacheImpl(SessionUtils sessionUtils, NodeDao nodeDao, long nodeIdToCriteriaMaxCacheSize) {
        //noinspection NullableProblems
        nodeIdToCriteriaCache = CacheBuilder.newBuilder()
                .maximumSize(nodeIdToCriteriaMaxCacheSize)
                .build(new CacheLoader<Long, NodeCriteria>() {
                    public NodeCriteria load(Long nodeId) {
                        return sessionUtils.withTransaction(() -> {
                            Objects.requireNonNull(nodeId);
                            final OnmsNode node = nodeDao.get(nodeId.intValue());
                            if (node != null && node.getForeignId() != null && node.getForeignSource() != null) {
                                return ImmutableNodeCriteria.newBuilder()
                                        .setId(nodeId.intValue())
                                        .setForeignId(node.getForeignId())
                                        .setForeignSource(node.getForeignSource())
                                        .build();
                            } else {
                                return ImmutableNodeCriteria.newBuilder()
                                        .setId(nodeId.intValue())
                                        .build();
                            }
                        });
                    }
                });
    }

    @Override
    public Optional<NodeCriteria> getNodeCriteria(Long nodeId) {
        Objects.requireNonNull(nodeId);

        try {
            return Optional.of(nodeIdToCriteriaCache.get(nodeId));
        } catch (Exception ignore) {
        }

        return Optional.empty();
    }
}
