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
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologyPersister;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.enlinkd.api.ReloadableTopologyDaemon;

/**
 * Deletes the generated topology via karaf command. The topology is identified as it belongs to the category "GeneratedNode"
 * Install: feature:install opennms-enlinkd-shell
 * Usage: typpe enlinkd:delete-topology in karaf console
 */
@Command(scope = "enlinkd", name = "delete-topology",
        description = "Delete generated topology (OnmsNodes, XxElements, XxLinks, SnmpInterfaces, IpInterfaces." +
                "The topology is identified as it belongs to the category 'GeneratedNode'")
@Service
public class DeleteTopologyCommand implements Action {

    @Reference
    private GenericPersistenceAccessor genericPersistenceAccessor;

    @Reference
    private ReloadableTopologyDaemon reloadableTopologyDaemon;

    @Override
    public Object execute() {
        // We print directly to System.out so it will appear in the console
        TopologyGenerator.ProgressCallback progressCallback = new TopologyGenerator.ProgressCallback(System.out::println);

        TopologyGenerator generator = TopologyGenerator.builder()
                .persister(new TopologyPersister(genericPersistenceAccessor, progressCallback))
                .progressCallback(progressCallback)
                .build();
        generator.deleteTopology();
        reloadableTopologyDaemon.reloadTopology();
        return null;
    }
}
