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
package org.opennms.features.topology.api.info;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Supplier;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.info.item.InfoPanelItem;

/**
 * Provider Interface to provide one to multiple {@link InfoPanelItem}s.
 */
public interface InfoPanelItemProvider {

    /**
     * Returns all contributions for the current state of the given container.
     *
     * @param container the container used to decide which contributions to show
     *
     * @return a bunch of contributions, if any. Should NEVER return null
     */
    Collection<? extends InfoPanelItem> getContributions(GraphContainer container);

    /**
     * Helper method to create the Contributions Collection.
     *
     * If <code>predicate</code> is <code>true</code> a singleton Collection with the value of {@link Supplier#get()} is returned.
     * If <code>predicate</code> is <code>false</code> an empty Collection is returned, instead.
     *
     * @param predicate Whether or not the value of {@link Supplier#get()} is used (<code>true</code> means it is used)
     * @param supplier The supplier to use. If <code>predicate</code> is <code>true</code> it must NOT be null
     * @param <T> The type of the {@link InfoPanelItem}
     * @return a singleton Collection with the value of {@link Supplier#get()} if <code>predicate</code> is <code>true</code>, otherwise an empty collection
     */
    static <T extends InfoPanelItem> Collection<T> contributeIf(final boolean predicate,
                                                                final Supplier<T> supplier) {
        return predicate
               ? Collections.singleton(Objects.requireNonNull(supplier).get())
               : Collections.emptySet();
    }
}
