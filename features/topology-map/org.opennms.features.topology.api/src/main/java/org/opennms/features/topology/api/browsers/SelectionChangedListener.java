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
package org.opennms.features.topology.api.browsers;


import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Listener to deal with selection changes.
 * This should be used in combination with the {@link OnmsVaadinContainer} or {@link SelectionAwareTable}.
 * This allows the underlying container to filter based on the {@link Selection}'s returned restrictions.
 * In addition to that it no longer depends directly on Topology knowledge.
 */
public interface SelectionChangedListener {

    /**
     * The new selection.
     * Defines how to convert the current selection to a list of Restrictions.
     */
    interface Selection {

        /**
         * Dummy selection for "NO SELECTION".
         * This does not filter the underlying {@link OnmsVaadinContainer}
         */
        Selection NONE = new Selection() {
            @Override
            public List<Restriction> toRestrictions() {
                return Lists.newArrayList();
            }
        };

        /**
         * The list of restrictions to return.
         * Please note, that each element in the list is AND concatenated.
         *
         * @return The list of {@link Restriction} to return to use to filter the {@link OnmsVaadinContainer}.
         */
        List<Restriction> toRestrictions();

    }

    /**
     * A convenient {@link Selection} to create an in-Restriction for the provided {@link #selectedIds}.
     * If {@link #selectedIds} is null or empty, an empty list is returned (results in "NO SELECTION").
     * Please be aware that this requires a "id" attribute on the entity managed by {@link OnmsVaadinContainer}.
     *
     * @param <T> The Type of the "id" attribute.
     *
     */
    class IdSelection<T extends Serializable> implements Selection {

        /** The selected ids, e.g. Node Ids */
        private final Set<T> selectedIds;

        public IdSelection(Collection<T> selectedIds) {
            this.selectedIds = Sets.newHashSet(selectedIds);
        }

        @Override
        public List<Restriction> toRestrictions() {
            if (selectedIds != null && !selectedIds.isEmpty()) {
                return Lists.newArrayList(Restrictions.in("id", selectedIds));
            }
            return Lists.newArrayList(Restrictions.isNull("id"));  // is always false, so nothing is shown
        }
    }

    /**
     * A convenient {@link Selection} to create an in-Restriction for the provided {@link #selectedNodeIds}.
     * If {@link #selectedNodeIds} is null or empty, an empty list is returned (results in "NO SELECTION").
     * Please note that this requires a "node.id" attribute on the entity managed by {@link OnmsVaadinContainer}.
     * It's original intention is to be used only for a {@link OnmsVaadinContainer} managing OnmsAlarm entities.
     */
    class AlarmNodeIdSelection implements Selection {

        private final Set<Integer> selectedNodeIds;

        public AlarmNodeIdSelection(Collection<Integer> selectedNodeIds) {
            this.selectedNodeIds = Sets.newHashSet(selectedNodeIds);
        }

        @Override
        public List<Restriction> toRestrictions() {
            if (selectedNodeIds != null && !selectedNodeIds.isEmpty()) {
                return Lists.newArrayList(Restrictions.in("node.id", selectedNodeIds));
            }
            return Lists.newArrayList(Restrictions.isNull("node.id"));  // is always false, so nothing is shown
        }
    }

    /**
     * Provide a new {@link Selection} object if the selection has changed.
     *
     * @param newSelection The new selection.
     */
    void selectionChanged(Selection newSelection);
}
