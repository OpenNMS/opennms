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

import java.net.InetAddress;
import java.util.Set;

import org.opennms.netmgt.model.ResourcePath;

/**
 * <p>CollectionAgent interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface CollectionAgent {

    /**
     * <P>
     * Returns the address information for the interface.
     * </p>
     *
     * @return a {@link java.lang.Object} object.
     */
    InetAddress getAddress();

    /**
     * Retrieves the names of all available attributes.
     *
     * @return a {@link Set} that contains the name of all configured attributes
     */
    Set<String> getAttributeNames();

    /**
     * <P>
     * This method is used to return the object that is associated with the
     * property name. This is very similar to the java.util.Map get() method,
     * but requires that the lookup be performed using a String name. The object
     * may be of any instance that the monitor previous stored.
     * </P>
     *
     * <P>
     * If there is no matching object for the property key, then a null pointer
     * is returned to the application.
     * </P>
     *
     * @param property
     *            The key for the lookup.
     * @return The resulting value for the key, null if no value exist.
     * @exception java.lang.IllegalArgumentException
     *                Thrown if the passed key is empty or null.
     * @see java.util.Map#get(java.lang.Object)
     */
    <V> V getAttribute(String property);

    /**
     * <P>
     * This method is used to associate an object value with a textual key. If a
     * previous value was associated with the key then the old value is returned
     * to the caller. This is identical to the behavior defined by the
     * java.util.Map put() method. The only restriction is that the key must be
     * a java string instance.
     * </P>
     *
     * @param property
     *            The key
     * @param value
     *            The value to associate with the key
     * @return The object that was previously associated with the key. Null is
     *         returned if there was no previous value associated.
     * @exception java.lang.IllegalArgumentException
     *                Thrown if the property name is empty or null.
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    Object setAttribute(String property, Object value);

    /**
     * <p>isStoreByForeignSource</p>
     * 
     * @return a {@link java.lang.Boolean} object.
     */
    Boolean isStoreByForeignSource();
    
    /**
     * <p>getHostAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getHostAddress();

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    int getNodeId();

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getNodeLabel();

    /**
     * <p>getForeignSource</p>
     * 
     * @return a {@link java.lang.String} object.
     */
    String getForeignSource();
    
    /**
     * <p>getForeignId</p>
     * 
     * @return a {@link java.lang.String} object.
     */
    String getForeignId();

    /**
     * <p>getLocationName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getLocationName();

    ResourcePath getStorageResourcePath();

    /**
     * <p>getSavedSysUpTime</p>
     *
     * @return a long.
     */
    long getSavedSysUpTime();

    /**
     * <p>setSavedSysUpTime</p>
     *
     * @param sysUpTime a long.
     */
    void setSavedSysUpTime(long sysUpTime);

}
