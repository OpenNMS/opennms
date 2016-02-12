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

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.ReadOnlyBusinessService;
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyEdge;
import org.opennms.netmgt.bsm.service.model.functions.reduce.MostCritical;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;

import com.google.common.collect.Sets;

public class MockBusinessService implements ReadOnlyBusinessService {
    private final long m_id;
    private String m_name;
    private Set<ReadOnlyEdge> m_edges = Sets.newHashSet();

    public MockBusinessService(long id) {
        m_id = id;
    }

    @Override
    public Long getId() {
        return m_id;
    }

    public void setName(String name) {
        m_name = name;
    }

    @Override
    public String getName() {
        return m_name != null ? m_name : String.valueOf(m_id);
    }

    @Override
    public ReductionFunction getReduceFunction() {
        return new MostCritical();
    }

    public void setEdges(Set<ReadOnlyEdge> edges) {
        m_edges = edges;
    }

    public void addEdge(ReadOnlyEdge edge) {
        m_edges.add(edge);
    }

    @Override
    public Set<ReadOnlyEdge> getEdges() {
        return m_edges;
    }

    @Override
    public String toString() {
        return String.format("MockBusinessService[id=%d]", m_id);
    }
}
