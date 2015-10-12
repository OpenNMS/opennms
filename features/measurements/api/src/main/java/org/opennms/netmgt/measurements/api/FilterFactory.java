/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.api;

import org.opennms.netmgt.measurements.model.FilterDef;

/**
 * Used to instantiate a {@link org.opennms.netmgt.measurements.api.Filter}
 * from the corresponding filter definition.
 *
 * @author jwhite
 */
public interface FilterFactory {
    /**
     * Retrieves the appropriate {@link Filter} for the given configuration.
     *
     * @param filterDef
     *   a filter definition
     * @return
     *   null if this factory doesn't support the filter in question
     */
    Filter getFilter(FilterDef filterDef);

    /**
     * Retrieves a reference to the associated {@link Filter} type.
     *
     * @return
     *   the filter configuration 
     */
    Class<? extends Filter> getFilterType();
}
