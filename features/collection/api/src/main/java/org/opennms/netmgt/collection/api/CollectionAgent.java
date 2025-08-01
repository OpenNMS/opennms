/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
