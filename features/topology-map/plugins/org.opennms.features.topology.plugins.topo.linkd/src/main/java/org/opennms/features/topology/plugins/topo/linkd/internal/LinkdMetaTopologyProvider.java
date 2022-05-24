/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.simple.SimpleMetaTopologyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LinkdMetaTopologyProvider extends SimpleMetaTopologyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(LinkdMetaTopologyProvider.class);

    private final List<GraphProvider> m_graphProviders = new ArrayList<>();

    public LinkdMetaTopologyProvider(LinkdTopologyProvider defaultlinkdTopologyProvider, List<LinkdTopologyProvider> linkdTopologyProviders) {
        super(Objects.requireNonNull(defaultlinkdTopologyProvider));
        m_graphProviders.add(defaultlinkdTopologyProvider);
        m_graphProviders.addAll(Objects.requireNonNull(linkdTopologyProviders));
    }

    @Override
    public List<GraphProvider> getGraphProviders() {
        return m_graphProviders;
    }

}