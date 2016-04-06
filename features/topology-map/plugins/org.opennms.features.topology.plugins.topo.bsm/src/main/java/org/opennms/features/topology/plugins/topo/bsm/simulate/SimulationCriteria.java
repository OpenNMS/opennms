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

package org.opennms.features.topology.plugins.topo.bsm.simulate;

import org.opennms.features.topology.api.NamespaceAware;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesTopologyProvider;

public abstract class SimulationCriteria extends Criteria implements NamespaceAware {
    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace().equals(namespace);
    }

    @Override
    public ElementType getType() {
        return ElementType.EDGE;
    }

    @Override
    public String getNamespace() {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && getClass().equals(obj.getClass());
    }
}
