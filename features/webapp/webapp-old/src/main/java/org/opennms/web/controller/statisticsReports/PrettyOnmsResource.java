/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2008 Feb 16: Created this file. - jeffg@opennms.org
 * 
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 */

package org.opennms.web.controller.statisticsReports;

import org.opennms.netmgt.model.OnmsResource;

/**
 * @author jeffg
 * This class extends OnmsResource and overrides the toString() method, providing
 * a more human-readable description of the resource.
 *
 */
public class PrettyOnmsResource extends OnmsResource {
    public PrettyOnmsResource(OnmsResource rs) {
        super(rs.getName(), rs.getLabel(), rs.getResourceType(), rs.getAttributes(), rs.getChildResources());
    }
    
    public String toString() {
        return this.getResourceType().getLabel() + ": " + this.getLabel();
    }
}
