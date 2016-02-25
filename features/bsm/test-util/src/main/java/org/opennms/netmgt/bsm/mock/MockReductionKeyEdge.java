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

import java.util.Collections;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;

public class MockReductionKeyEdge implements ReadOnlyReductionKeyEdge {

    private final Long m_id;
    private final String m_reductionKey;
    private final String m_friendlyName;

    public MockReductionKeyEdge(long id, String reductionKey, String friendlyName) {
        m_id = id;
        m_reductionKey = reductionKey;
        m_friendlyName = friendlyName;
    }

    @Override
    public Long getId() {
        return m_id;
    }

    @Override
    public Type getType() {
        return Type.REDUCTION_KEY;
    }

    @Override
    public String getReductionKey() {
        return m_reductionKey;
    }

    @Override
    public Set<String> getReductionKeys() {
        return Collections.singleton(m_reductionKey);
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
    public String getFriendlyName() {
        return m_friendlyName;
    }
}
