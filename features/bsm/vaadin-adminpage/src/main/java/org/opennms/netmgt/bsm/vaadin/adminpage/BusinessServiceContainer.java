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
package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Maps;
import com.vaadin.v7.data.util.BeanContainer;

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
