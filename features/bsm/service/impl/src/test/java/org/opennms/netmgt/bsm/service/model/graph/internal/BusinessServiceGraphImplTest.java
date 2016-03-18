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

package org.opennms.netmgt.bsm.service.model.graph.internal;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.netmgt.bsm.mock.MockBusinessServiceHierarchy;
import org.opennms.netmgt.bsm.mock.MockBusinessServiceHierarchy.Builder;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BusinessServiceGraphImplTest {

    @Test
    public void canCalculateVertexLevel()  {
        /**
         * Creates a graph that looks like:
         *   B1    B5   B6   B7
         *    |   /     /
         *    B2  /    /
         *    | /   /
         *    B3   /
         *    | /
         *    B4
         */
        MockBusinessServiceHierarchy h = MockBusinessServiceHierarchy.builder()
                .withBusinessService(1)
                .withBusinessService(2)
                    .withBusinessService(3)
                        .withBusinessService(4).commit()
                    .commit()
                .commit()
            .commit()
            .withBusinessService(5)
                .withBusinessService(3).commit()
            .commit()
            .withBusinessService(6)
                .withBusinessService(4).commit()
            .commit()
            .withBusinessService(7).commit()
            .build();
        List<BusinessService> businessServices = h.getBusinessServices();

        // Quick sanity check of the generated hierarchy
        assertEquals(7, businessServices.size());
        assertEquals(1, h.getBusinessServiceById(3).getEdges().size());

        // Create the graph
        BusinessServiceGraph graph = new BusinessServiceGraphImpl(h.getBusinessServices());

        // Verify the services at every level
        Map<Integer, Set<BusinessService>> servicesByLevel = Maps.newTreeMap();
        servicesByLevel.put(0, Sets.newHashSet(h.getBusinessServiceById(1), h.getBusinessServiceById(5), h.getBusinessServiceById(6), h.getBusinessServiceById(7)));
        servicesByLevel.put(1, Sets.newHashSet(h.getBusinessServiceById(2)));
        servicesByLevel.put(2, Sets.newHashSet(h.getBusinessServiceById(3)));
        servicesByLevel.put(3, Sets.newHashSet(h.getBusinessServiceById(4)));
        servicesByLevel.put(4, Sets.newHashSet());
        for (Entry<Integer, Set<BusinessService>> entry : servicesByLevel.entrySet()) {
            int level = entry.getKey();
            Set<BusinessService> servicesAtLevel = graph.getVerticesByLevel(level).stream()
                .filter(v -> v.getLevel() == level) // Used to verify the level on the actual vertex
                .map(v -> v.getBusinessService())
                .collect(Collectors.toSet());
            assertEquals(String.format("Mismatch at level %d",  level), entry.getValue(), servicesAtLevel);
        }
    }

    @Test
    public void canCalculateVertexLevelForDeepHierarchy() {
        final String[][] BUSINESS_SERVICE_NAMES = new String[][]{
            {"b1"},
            {"b2"},
            {"b3", "c21", "c22"},
            {"b4", "c31", "c32", "c33"},
            {"b5", "c41"},
            {"b6", "c51", "c52"},
            {"b7", "c61"},
            {"b8", "c71", "c72", "c73", "c74"},
            {"b9", "c81", "c82"},
            {"b10", "c91", "c92", "c93", "c94", "c95", "c96", "c97", "c98", "c99"},
            {"b11", "c101"},
            {"b12", "c111", "c112"},
            {"b13", "c121"},
            {"b14"}
        };

        // Build the hierarchy, linking every level to the left most business service
        // in the line above
        Map<Long, Integer> businessServiceIdToLevel = Maps.newHashMap();
        Builder builder = MockBusinessServiceHierarchy.builder();
        long k = 0;
        for (int level = 0; level < BUSINESS_SERVICE_NAMES.length; level++) {
            String[] servicesAtLevel = BUSINESS_SERVICE_NAMES[level];
            for (int i = servicesAtLevel.length - 1; i >= 0; i--) {
                builder = builder.withBusinessService(k)
                    .withName(servicesAtLevel[i]);
                businessServiceIdToLevel.put(k, level);
                k++;
                if (i != 0) {
                    builder = builder.commit();
                }
            }
        }
        for (int level = 0; level < BUSINESS_SERVICE_NAMES.length; level++) {
            builder = builder.commit();
        }

        // Create the graph
        MockBusinessServiceHierarchy h = builder.build();
        BusinessServiceGraph graph = new BusinessServiceGraphImpl(h.getBusinessServices());

        // Verify
        for (Entry<Long, Integer> entry : businessServiceIdToLevel.entrySet()) {
            long id = entry.getKey();
            int expectedLevel = entry.getValue();
            assertEquals(expectedLevel, graph.getVertexByBusinessServiceId(id).getLevel());
        }
    }
}
