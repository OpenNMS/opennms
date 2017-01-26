/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api;

/**
 * Each {@link IconRepository} stores a mapping from icon keys to icon ids.
 * The icon key is Graph Provider specific and is defined by it (e.g. "sfree:group")
 * The icon id should match with an id element in all existing SVGs (not only this {@link IconRepository}.
 */
public interface IconRepository {

    /**
     * Verifies if a mapping for the provided icon Key is defined.
     * @param iconKey the icon key
     * @return true if a mapping is defined, false otherwise.
     */
    boolean contains(String iconKey);

    /**
     * Maps the provided <code>iconKey</code> to an SVG id element.
     * If no mapping is defined, <code>null</code> is returned.
     *
     * @param iconKey The icon key to look up
     * @return The icon id, or null if no mapping is defined.
     */
    String getSVGIconId(String iconKey);

    /**
     * Adds a custom icon mapping. The <code>iconKey</code> must be unique in this {@link IconRepository}.
     *
     * @param iconKey The icon key
     * @param iconId The icon id for the icon key
     */
    void addIconMapping(String iconKey, String iconId);

    /**
     * Removes the given <code>iconKey</code> from this {@link IconRepository}.
     *
     * @param iconKey The icon key to remove
     */
    void removeIconMapping(String iconKey);

    /**
     * Persists this {@link IconRepository}.
     * Should be invoked if changes to this {@link IconRepository} should be persisted permanently (e.g. on disk)
     */
    void save();
}
