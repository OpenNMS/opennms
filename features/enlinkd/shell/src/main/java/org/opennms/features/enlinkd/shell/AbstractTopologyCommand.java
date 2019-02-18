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

import java.util.Date;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.opennms.enlinkd.generator.TopologyContext;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologyPersister;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;

abstract class AbstractTopologyCommand implements Action {
    @Reference
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Reference
    private EventIpcManager eventIpcManager;

    protected TopologyGenerator createTopologyGenerator(){
        // We print directly to System.out so it will appear in the console
        TopologyGenerator.ProgressCallback progressCallback = new TopologyGenerator.ProgressCallback(System.out::println);

        TopologyPersister persister = new TopologyPersister(genericPersistenceAccessor, progressCallback);

        TopologyContext.Action action = new TopologyContext.Action() {
            @Override
            public void invoke() throws Exception {
                final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_TOPOLOGY_UEI, getClass().getSimpleName());
                builder.setTime(new Date()); builder.setParam(EventConstants.PARAM_TOPOLOGY_NAMESPACE, "all");
                eventIpcManager.send(builder.getEvent());
            }
        };

        TopologyContext context = TopologyContext.builder()
                .topologyPersister(persister)
                .progressCallback(progressCallback)
                .addPostAction(action)
                .build();

        return new TopologyGenerator(context);
    }
}
