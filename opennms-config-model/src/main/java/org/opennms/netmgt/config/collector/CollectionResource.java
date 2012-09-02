/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.config.collector;

import org.opennms.core.utils.TimeKeeper;

/**
 * <p>CollectionResource interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionResource extends ResourceIdentifier {
    /**
     * <p>shouldPersist</p>
     *
     * @param params a {@link org.opennms.netmgt.collectd.ServiceParameters} object.
     * @return a boolean.
     */
    public boolean shouldPersist(ServiceParameters params);
    
    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    public boolean rescanNeeded();
    
    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.collectd.CollectionSetVisitor} object.
     */
    public void visit(CollectionSetVisitor visitor);
    
    /**
     * Returns something like an ifType; is (but not sure if it should be) -1 for non interface type collections, otherwise
     * the SNMP type of the interface. Relates to ifType in datacollection-config.xml
     *
     * @return a int.
     */
    public int getType();
    
    /**
     * Returns a string which indicates what type of resource.
     * Will be one of
     *          "node" for the node level resource
     *          "if" for network interface resources
     *          "*" for all other resource types defined in the relevant config files, e.g. hrStorage
     *
     * @return a {@link java.lang.String} object.
     */
    public String getResourceTypeName();
    
    /**
     * Returns the name of the parent resource.
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getParent();
    
    /**
     * Returns the name of the instance this CollectionResource represents.  For node level resources, this will be null
     * to indicate the default instance.   For interface level resources, some label unique to the node (ifIndex probably)
     * For Generic resources (e.g. the SNMP GenericIndexResource), this will be some identifying label, probably the index in the table
     *
     * @return a {@link java.lang.String} object.
     */
    public String getInstance();

    /**
     * Returns a unique label for each resource depending on resource type.
     * This label is the same label used when constructing the resource ID.
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLabel();

    /**
     * Returns a not-null time tracker instance when this resource requires to use a special timestamp when updating RRDs/JRBs.
     * If the resource doesn't need a special time tracker it should return null.
     * 
     * @return a {@link org.opennms.core.utils.TimeKeeper} object.
     */
    public TimeKeeper getTimeKeeper();

}
