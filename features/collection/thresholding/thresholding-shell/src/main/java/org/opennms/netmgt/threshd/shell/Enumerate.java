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

package org.opennms.netmgt.threshd.shell;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

@Command(scope = "opennms-threshold-states", name = "enumerate", description = "Enumerates threshold states")
@Service
public class Enumerate extends AbstractThresholdStateCommand {
    @Reference
    private NodeDao nodeDao;

    @Option(name = "-n", aliases = "--node", description = "A node to filter by in the form of either the node's ID " +
            "or foreign_source:foreign_id")
    private String node;

    @Override
    public Object execute() {
        AtomicInteger index = new AtomicInteger(1);
        Map<Integer, String> stateKeyIndexes = blobStore.enumerateContext(THRESHOLDING_KV_CONTEXT)
                .entrySet()
                .stream()
                .filter(getFilter())
                .collect(Collectors.toMap(e -> index.getAndIncrement(), Map.Entry::getKey));

        if (stateKeyIndexes.isEmpty()) {
            System.out.println("No threshold states found");
        } else {
            System.out.println("Index\tState Key");
            stateKeyIndexes.forEach((k, v) -> System.out.println(k + "\t" + v));
            session.put(STATE_INDEXES_SESSION_KEY, stateKeyIndexes);
        }

        return null;
    }

    private Predicate<Map.Entry<String, byte[]>> getFilter() {
        AtomicInteger nodeIdToFilter = new AtomicInteger(-1);

        if (node != null) {
            try {
                nodeIdToFilter.set(Integer.parseInt(node));

                if (nodeIdToFilter.get() < 0) {
                    throw new IllegalStateException("Node ID must be positive");
                }

                OnmsNode nodeFound = nodeDao.get(node);

                if (nodeFound == null) {
                    throw new IllegalArgumentException(String.format("No node with ID '%s' could be found", node));
                }
            } catch (NumberFormatException ignore) {
                OnmsNode nodeFound = nodeDao.get(node);

                if (nodeFound == null) {
                    throw new IllegalArgumentException(String.format("No node with foreign_source:foreign_id '%s' " +
                            "could be found", node));
                }

                nodeIdToFilter.set(nodeFound.getId());
            }
        }

        return entry -> {
            int nodeId = nodeIdToFilter.get();

            if (nodeId >= 0) {
                return entry.getKey().startsWith(nodeId + "-");
            }

            return true;
        };
    }
}
