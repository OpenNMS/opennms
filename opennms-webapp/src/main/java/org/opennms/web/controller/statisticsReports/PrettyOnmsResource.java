/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.statisticsReports;

import org.opennms.netmgt.model.OnmsResource;

/**
 * <p>PrettyOnmsResource class.</p>
 *
 * @author jeffg
 * This class extends OnmsResource and overrides the toString() method, providing
 * a more human-readable description of the resource.
 * @version $Id: $
 * @since 1.8.1
 */
public class PrettyOnmsResource extends OnmsResource {
    /**
     * <p>Constructor for PrettyOnmsResource.</p>
     *
     * @param rs a {@link org.opennms.netmgt.model.OnmsResource} object.
     */
    public PrettyOnmsResource(OnmsResource rs) {
        super(rs.getName(), rs.getLabel(), rs.getResourceType(), rs.getAttributes(), rs.getChildResources());
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return this.getResourceType().getLabel() + ": " + this.getLabel();
    }
}
