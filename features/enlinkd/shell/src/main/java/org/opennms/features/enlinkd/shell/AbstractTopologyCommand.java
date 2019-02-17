/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.enlinkd.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologyPersister;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.enlinkd.BridgeOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.CdpOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.IsisOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.LldpOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.NodesOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.OspfOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;

abstract class AbstractTopologyCommand implements Action {
    @Reference
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Reference
    private LinkdTopologyProvider linkdTopologyProvider;

    @Reference
    private TopologyEntityCache entityCache;

    @Reference
    private NodesOnmsTopologyUpdater nodesOnmsTopologyUpdater;

    @Reference
    private CdpOnmsTopologyUpdater cdpOnmsTopologyUpdater;

    @Reference
    private IsisOnmsTopologyUpdater isisOnmsTopologyUpdater;

    @Reference
    private LldpOnmsTopologyUpdater lldpOnmsTopologyUpdater;

    @Reference
    private OspfOnmsTopologyUpdater ospfOnmsTopologyUpdater;

    @Reference
    private BridgeOnmsTopologyUpdater bridgeOnmsTopologyUpdater;

    protected TopologyGenerator createTopologyGenerator(){
        // We print directly to System.out so it will appear in the console
        TopologyGenerator.ProgressCallback progressCallback = new TopologyGenerator.ProgressCallback(System.out::println);

        TopologyPersister persister = new TopologyPersister(genericPersistenceAccessor, progressCallback);
        TopologyContext context = TopologyContext.builder()
                .topologyPersister(persister)
                .progressCallback(progressCallback)
                // we need to inject the Updaters this way to not create a circular dependency
                .addPostAction(() -> nodesOnmsTopologyUpdater.setTopology(nodesOnmsTopologyUpdater.buildTopology()))
                .addPostAction(() -> cdpOnmsTopologyUpdater.setTopology(cdpOnmsTopologyUpdater.buildTopology()))
                .addPostAction(() -> isisOnmsTopologyUpdater.setTopology(isisOnmsTopologyUpdater.buildTopology()))
                .addPostAction(() -> lldpOnmsTopologyUpdater.setTopology(lldpOnmsTopologyUpdater.buildTopology()))
                .addPostAction(() -> ospfOnmsTopologyUpdater.setTopology(ospfOnmsTopologyUpdater.buildTopology()))
                .addPostAction(() -> bridgeOnmsTopologyUpdater.setTopology(bridgeOnmsTopologyUpdater.buildTopology()))
                .addPostAction(() -> entityCache.refresh())
                .addPostAction(() -> linkdTopologyProvider.refresh())
                .build();

        return new TopologyGenerator(context);
    }
}
