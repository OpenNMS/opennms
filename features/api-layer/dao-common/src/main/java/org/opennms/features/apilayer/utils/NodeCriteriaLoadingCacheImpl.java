/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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
