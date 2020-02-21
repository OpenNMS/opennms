/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.topology;

import java.util.Objects;

import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public class LegacyTopologyProviderInfo implements TopologyProviderInfo {

    private final GraphInfo delegate;

    public LegacyTopologyProviderInfo(final GenericGraph genericGraph) {
        this(Objects.requireNonNull(genericGraph).getGraphInfo());
    }

    public LegacyTopologyProviderInfo(final GraphInfo graphInfo) {
        this.delegate = Objects.requireNonNull(graphInfo);
    }

    @Override
    public String getName() {
        return delegate.getLabel();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public boolean isHierarchical() {
        return false;
    }

    @Override
    public boolean isSupportsCategorySearch() {
        return false;
    }
}
