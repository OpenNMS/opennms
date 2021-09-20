/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.bsm.browsers;

import org.opennms.features.topology.api.browsers.OnmsVaadinContainer;
import org.opennms.features.topology.api.browsers.SelectionAwareTable;

@SuppressWarnings("rawtypes")
public class BusinessServicesTable extends SelectionAwareTable {
    private static final long serialVersionUID = 1L;

    /**
     * Leave OnmsVaadinContainer without generics; the Aries blueprint code cannot
     * match up the arguments if you put the generic types in.
     */
    public BusinessServicesTable(final String caption, final OnmsVaadinContainer container) {
        super(caption, container);
    }
}
