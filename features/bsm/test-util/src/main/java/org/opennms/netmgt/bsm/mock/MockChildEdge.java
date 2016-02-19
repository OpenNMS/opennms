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
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyChildEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;

import com.google.common.collect.Sets;

public class MockChildEdge implements ReadOnlyChildEdge {
    private final Long m_id;
    private final ReadOnlyBusinessService m_businessService;

    public MockChildEdge(long id, ReadOnlyBusinessService businessService) {
        m_id = id;
        m_businessService = businessService;
    }

    @Override
    public Long getId() {
        return m_id;
    }

    @Override
    public Type getType() {
        return Type.CHILD_SERVICE;
    }

    @Override
    public Set<String> getReductionKeys() {
        return Sets.newHashSet();
    }

    @Override
    public MapFunction getMapFunction() {
        return new Identity();
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public ReadOnlyBusinessService getChild() {
        return m_businessService;
    }

    @Override
    public String toString() {
        return String.format("MockChildEdge[id=%d, businessService=%s]", m_id, m_businessService);
    }
}
