/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.api;

import org.opennms.netmgt.collection.support.DefaultTimeKeeper;
import org.opennms.netmgt.model.ResourcePath;

/**
 * <p>CollectionResource interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionResource extends ResourceIdentifier, CollectionVisitable, Persistable {

    public static final String RESOURCE_TYPE_NODE = "node";
    public static final String RESOURCE_TYPE_IF = "if";

    /**
     * <p>rescanNeeded</p>
     *
     * @return a boolean.
     */
    boolean rescanNeeded();
    
    /**
     * Returns a string which indicates what type of resource.
     * Will be one of
     *          "node" for the node level resource
     *          "if" for network interface resources
     *          "*" for all other resource types defined in the relevant config files, e.g. hrStorage
     *
     * @return a {@link java.lang.String} object.
     */
    String getResourceTypeName();
    
    /**
     * Returns the name of the parent resource.
     * 
     * @return a {@link java.lang.String} object.
     */
    ResourcePath getParent();
    
    /**
     * Returns the name of the instance this {@link CollectionResource} represents. For node level resources, this will be null
     * to indicate the default instance. For interface level resources, some label unique to the node (ifIndex probably).
     * For Generic resources (e.g. the SNMP {@link GenericIndexResource}), this will be some identifying label, probably the index in the table.
     * This value is used by the {@link StorageStrategy} implementations to figure out the label for the resource which 
     * is used in constructing its RRD directory.
     *
     * @return a {@link java.lang.String} object.
     */
    String getInstance();

    /**
     * Returns a unique label for each resource depending on resource type.
     * This label is the same label used when constructing the resource ID.
     *
     * @return a {@link java.lang.String} object.
     */
    String getInterfaceLabel();

    /**
     * Returns a not-null {@link TimeKeeper} instance when this resource requires to use a special timestamp when updating RRDs/JRBs.
     * If the resource doesn't need a special {@link TimeKeeper} it should return null.
     * 
     * @return a {@link org.opennms.netmgt.collection.api.TimeKeeper} object or null to indicate that {@link DefaultTimeKeeper} should be used.
     */
    TimeKeeper getTimeKeeper();

}
