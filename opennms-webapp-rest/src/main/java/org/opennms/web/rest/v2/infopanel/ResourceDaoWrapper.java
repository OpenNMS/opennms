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
package org.opennms.web.rest.v2.infopanel;

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;

/**
 * Read-only wrapper around {@link ResourceDao} placed into the Jinjava template
 * context used by {@link InfoPanelRenderer}. Only query/lookup methods are
 * delegated; the mutating {@code deleteResourceById} is not exposed.
 *
 * <p>Mirrors the legacy topology map's wrapper of the same name so existing
 * {@code etc/infopanel/} templates render unchanged.
 */
public class ResourceDaoWrapper {

    private final ResourceDao delegate;

    public ResourceDaoWrapper(final ResourceDao delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    /**
     * Retrieve a resource by its unique {@link ResourceId}
     * (e.g. {@code node[1].nodeSnmp[]}), or {@code null} if not found.
     */
    public OnmsResource getResourceById(final ResourceId id) {
        return delegate.getResourceById(id);
    }

    /** Retrieve the resource tree rooted at the given node (children populated). */
    public OnmsResource getResourceForNode(final OnmsNode node) {
        return delegate.getResourceForNode(node);
    }

    /**
     * Return all top-level resources (typically one per monitored node).
     * Can be expensive -- use with care in templates.
     */
    public List<OnmsResource> findTopLevelResources() {
        return delegate.findTopLevelResources();
    }
}
