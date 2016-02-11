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

public interface SelectionChangedListener {

    interface Selection {

        Selection EMPTY = new Selection() {
            @Override
            public List<Restriction> toRestrictions() {
                return Lists.newArrayList();
            }
        };

        List<Restriction> toRestrictions();

    }

    class IdSelection<T extends Serializable> implements Selection {

        private final Set<T> selectedIds;

        public IdSelection(Collection<T> selectedIds) {
            this.selectedIds = Sets.newHashSet(selectedIds);
        }

        @Override
        public List<Restriction> toRestrictions() {
            if (selectedIds != null && !selectedIds.isEmpty()) {
                return Lists.newArrayList(Restrictions.in("id", selectedIds));
            }
            return Lists.newArrayList();
        }
    }

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
            return Lists.newArrayList();
        }
    }

    void selectionChanged(Selection newSelection);
}
