/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
