/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 *      http://www.opennms.com/
 *
 */

package org.opennms.netmgt.collectd;

public interface CollectionResource extends ResourceIdentifier {
    public boolean shouldPersist(ServiceParameters params);
    
    public boolean rescanNeeded();
    
    public void visit(CollectionSetVisitor visitor);
    
    /**
     * Returns something like an ifType; is (but not sure if it should be) -1 for non interface type collections, otherwise
     * the SNMP type of the interface. Relates to ifType in datacollection-config.xml
     * @return
     */
    public int getType();
    
    /**
     * Returns a string which indicates what type of resource.  
     * Will be one of 
     *          "node" for the node level resource
     *          "if" for network interface resources
     *          "*" for all other resource types defined in the relevant config files, e.g. hrStorage
     * @return
     */
    public String getResourceTypeName();
    
    
    /**
     * Returns the name of the instance this CollectionResource represents.  For node level resources, this will be null
     * to indicate the default instance.   For interface level resources, some label unique to the node (ifIndex probably)
     * For Generic resources (e.g. the SNMP GenericIndexResource), this will be some identifying label, probably the index in the table
     * @return
     */
    public String getInstance();

    /**
     * Returns a unique label for each resource depending on resource type.
     * This label is the same label used when constructing the resource ID.
     * @Return
     */
    public String getLabel();
}
