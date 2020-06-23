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

package org.opennms.netmgt.measurements.filters.impl;

import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.api.FilterFactory;
import org.opennms.netmgt.measurements.model.FilterDef;

import com.google.common.collect.RowSortedTable;

/**
 * A no-op filter used for testing.
 *
 * @author jwhite
 */
public class NoOpFilter implements FilterFactory {

    public static final String FILTER_NAME = "NoOp";

    @Override
    public Filter getFilter(FilterDef filterDef) {
        if (FILTER_NAME.equalsIgnoreCase(filterDef.getName())) {
            return new Filter() {
                @Override
                public void filter(RowSortedTable<Long, String, Double> dsAsTable) {
                    // pass
                }
            };
        };
        return null;
    }

    @Override
    public Class<? extends Filter> getFilterType() {
        return null;
    }
}
