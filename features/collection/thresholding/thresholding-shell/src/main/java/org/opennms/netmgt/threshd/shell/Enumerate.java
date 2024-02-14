/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

@Command(scope = "opennms", name = "threshold-enumerate", description = "Enumerates threshold states")
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
