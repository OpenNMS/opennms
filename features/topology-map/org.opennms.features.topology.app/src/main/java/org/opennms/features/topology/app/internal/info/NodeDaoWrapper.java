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
package org.opennms.features.topology.app.internal.info;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;

/**
 * A read-only wrapper around {@link NodeDao} designed to be placed into
 * the Jinjava template context used by {@link GenericInfoPanelItemProvider}.
 *
 * <p>Only query/lookup methods are delegated. All mutating operations
 * inherited from {@code OnmsDao} — {@code save}, {@code update},
 * {@code delete}, {@code saveOrUpdate}, {@code flush}, {@code clear},
 * and {@code lock} — are deliberately <b>not</b> exposed.</p>
 *
 * <p>Because Jinjava resolves methods via reflection on the runtime class,
 * keeping this as a concrete POJO (rather than a subclass of {@code NodeDao})
 * guarantees that only the methods declared here are reachable from
 * template expressions.</p>
 *
 * @see org.opennms.features.topology.api.info.MeasurementsWrapper
 */
public class NodeDaoWrapper {

    private final NodeDao delegate;

    public NodeDaoWrapper(final NodeDao delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    /** Look up a node by its integer database ID. */
    public OnmsNode get(final Integer id) {
        return delegate.get(id);
    }

    /** Look up a node by criteria string (nodeId or foreignSource:foreignId). */
    public OnmsNode get(final String lookupCriteria) {
        return delegate.get(lookupCriteria);
    }

    /** Load a node by ID; throws if not found. */
    public OnmsNode load(final Integer id) {
        return delegate.load(id);
    }

    /** Map of every nodeId → nodeLabel. Cheaper than {@code findAll()}. */
    public Map<Integer, String> getAllLabelsById() {
        return delegate.getAllLabelsById();
    }

    /** Return just the label for one node. */
    public String getLabelForId(final Integer id) {
        return delegate.getLabelForId(id);
    }

    /** Return the monitoring-location name for one node. */
    public String getLocationForId(final Integer id) {
        return delegate.getLocationForId(id);
    }

    /** Return all nodes (expensive on large systems — use sparingly). */
    public List<OnmsNode> findAll() {
        return delegate.findAll();
    }

    /** Find nodes whose label matches exactly. */
    public List<OnmsNode> findByLabel(final String label) {
        return delegate.findByLabel(label);
    }

    /** Count all nodes. */
    public int countAll() {
        return delegate.countAll();
    }

    /** Count nodes matching the supplied {@link Criteria}. */
    public int countMatching(final Criteria criteria) {
        return delegate.countMatching(criteria);
    }

    /** Return nodes matching the supplied {@link Criteria}. */
    public List<OnmsNode> findMatching(final Criteria criteria) {
        return delegate.findMatching(criteria);
    }

    /** Map of foreignId → nodeId for every node that has a foreignSource. */
    public Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource) {
        return delegate.getForeignIdToNodeIdMap(foreignSource);
    }

    /** Map of foreignSource → set-of-foreignIds. */
    public Map<String, java.util.Set<String>> getForeignIdsPerForeignSourceMap() {
        return delegate.getForeignIdsPerForeignSourceMap();
    }

}
