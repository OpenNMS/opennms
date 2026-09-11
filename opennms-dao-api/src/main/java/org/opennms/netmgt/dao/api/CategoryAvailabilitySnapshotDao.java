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
package org.opennms.netmgt.dao.api;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.availability.NodeAvailability;

/**
 * Persists the most recent availability figures per category so that the
 * web tier and the REST API can read them without recomputing. There is one
 * snapshot per category label; saving replaces the previous one atomically.
 */
public interface CategoryAvailabilitySnapshotDao {

    /**
     * Replace the snapshot for the category named by the given result's label.
     * The result must carry its node list.
     */
    void save(CategoryAvailability availability);

    /** Headline figures for one category, without its node list. */
    Optional<CategoryAvailability> findSummary(String label);

    /** Headline figures for every category that has a snapshot, sorted by label. */
    List<CategoryAvailability> findAllSummaries();

    /** One category with every member node loaded. Avoid on very large categories when a page will do. */
    Optional<CategoryAvailability> findWithNodes(String label);

    /**
     * A page of a category's nodes sorted by node ID.
     *
     * @param offset rows to skip
     * @param limit  maximum rows to return; zero or negative returns every remaining row
     */
    List<NodeAvailability> findNodes(String label, int offset, int limit);

    /** One member node of a category, empty if the node is not in it. */
    Optional<NodeAvailability> findNode(String label, int nodeId);

    /** IDs of a category's member nodes, sorted. */
    List<Integer> findNodeIds(String label);

    void deleteByLabel(String label);

    /** Drop every snapshot whose label is not in the given set. */
    void retainOnly(Collection<String> labels);
}
