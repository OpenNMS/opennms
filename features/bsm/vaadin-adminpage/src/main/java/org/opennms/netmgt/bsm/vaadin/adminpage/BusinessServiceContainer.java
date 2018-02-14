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

package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Maps;
import com.vaadin.data.util.BeanContainer;

/**
 * Container which holds {@link BusinessServiceRow} objects.
 * The bean id is the {@link BusinessServiceRow#getRowId()} property.
 */
public class BusinessServiceContainer extends BeanContainer<Long, BusinessServiceRow> {

    private final AtomicLong rowIdCounter = new AtomicLong(0);

    private final Map<Long, Long> rowIdToParentRowIdMapping = Maps.newHashMap();

    public BusinessServiceContainer() {
        super(BusinessServiceRow.class);
        setBeanIdProperty("rowId");
    }

    public void addRow(BusinessServiceGraph graph, GraphVertex v) {
        createRowForVertex(graph, v, null);
    }

    private void createRowForVertex(BusinessServiceGraph graph, GraphVertex graphVertex, BusinessServiceRow parentRow) {
        final BusinessService businessService = graphVertex.getBusinessService();
        if (businessService == null) {
            return;
        }

        final long rowId = rowIdCounter.incrementAndGet();
        final Long parentBusinessServiceId = parentRow != null ? parentRow.getBusinessService().getId() : null;
        final BusinessServiceRow row = new BusinessServiceRow(rowId, businessService, parentBusinessServiceId);
        if (parentRow != null) {
            rowIdToParentRowIdMapping.put(rowId, parentRow.getRowId());
        }
        addBean(row);

        // Recurse with all of the children
        graph.getOutEdges(graphVertex).stream()
                .map(e -> graph.getOpposite(graphVertex, e))
                .filter(v -> v.getBusinessService() != null)
                .sorted((v1, v2) -> v1.getBusinessService().getName().compareTo(v2.getBusinessService().getName()))
                .forEach(v -> createRowForVertex(graph, v, row));
    }

    protected Map<Long, Long> getRowIdToParentRowIdMapping() {
        return Collections.unmodifiableMap(rowIdToParentRowIdMapping);
    }
}
