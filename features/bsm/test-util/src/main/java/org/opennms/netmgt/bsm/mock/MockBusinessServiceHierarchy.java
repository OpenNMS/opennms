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

package org.opennms.netmgt.bsm.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.mock.MockBusinessServiceHierarchy.HierarchyBuilder.BusinessServiceBuilder;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Fluent API for building hierarchies.
 *
 * @author jwhite
 */
public class MockBusinessServiceHierarchy {

    public static interface Builder {
        BusinessServiceBuilder withBusinessService(final long id);
        Builder commit();
        MockBusinessServiceHierarchy build();
    }

    public static class HierarchyBuilder implements Builder {
        private final Map<Long, MockBusinessService> m_businessServicesById = Maps.newTreeMap();

        public static class BusinessServiceBuilder implements Builder {
            private final HierarchyBuilder m_root;
            private final Builder m_parent;
            private final MockBusinessService m_businessService;
            private final Set<Edge> m_edges = Sets.newHashSet();

            private BusinessServiceBuilder(HierarchyBuilder root, Builder parent, MockBusinessService businessService) {
                m_root = root;
                m_parent = parent;
                m_businessService = businessService;
            }

            public BusinessServiceBuilder withName(String name) {
                m_businessService.setName(name);
                return this;
            }

            public BusinessServiceBuilder withReductionFunction(ReductionFunction reduce) {
                m_businessService.setReductionFunction(reduce);
                return this;
            }

            public BusinessServiceBuilder withReductionKey(long id, String reductionKey) {
                m_edges.add(new MockReductionKeyEdge(id, reductionKey, null));
                return this;
            }

            public BusinessServiceBuilder withBusinessService(long businessServiceId) {
                MockBusinessService childService = m_root.getOrCreateBusinessService(businessServiceId);
                m_edges.add(new MockChildEdge(businessServiceId, childService));
                return new BusinessServiceBuilder(m_root, this, childService);
            }

            @Override
            public MockBusinessServiceHierarchy build() {
                throw new UnsupportedOperationException("Build can only be invoked on the root.");
            }

            @Override
            public Builder commit() {
                for (Edge edge : m_edges) {
                    m_businessService.addEdge(edge);
                }
                m_root.m_businessServicesById.put(m_businessService.getId(), m_businessService);
                return m_parent;
            }
        }

        private MockBusinessService getOrCreateBusinessService(final long businessServiceId) {
            MockBusinessService businessService = m_businessServicesById.get(Long.valueOf(businessServiceId));
            if (businessService == null) {
                businessService = new MockBusinessService(businessServiceId);
                m_businessServicesById.put(businessServiceId, businessService);
            }
            return businessService;
        }

        public BusinessServiceBuilder withBusinessService(final long businessServiceId) {
            return new BusinessServiceBuilder(this, this, getOrCreateBusinessService(businessServiceId));
        }

        @Override
        public HierarchyBuilder commit() {
            return this;
        }

        @Override
        public MockBusinessServiceHierarchy build() {
            return new MockBusinessServiceHierarchy(this);
        }
    }

    public static HierarchyBuilder builder() {
        return new HierarchyBuilder();
    }

    private final HierarchyBuilder m_builder;

    private MockBusinessServiceHierarchy(HierarchyBuilder builder) {
        m_builder = builder;
    }

    public List<BusinessService> getBusinessServices() {
        return m_builder.m_businessServicesById.values().stream()
                    .map(b -> (BusinessService)b).collect(Collectors.toList());
    }

    public BusinessService getBusinessServiceById(long id) {
        return m_builder.m_businessServicesById.get(Long.valueOf(id));
    }

    public Edge getEdgeByReductionKey(String reductionKey) {
        return m_builder.m_businessServicesById.values().stream()
                .map(MockBusinessService::getEdges)
                .flatMap(Set::stream)
                .filter(e -> e.getReductionKeys().contains(reductionKey))
                .findFirst().orElse(null);
    }
}
