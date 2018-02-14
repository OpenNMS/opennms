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
