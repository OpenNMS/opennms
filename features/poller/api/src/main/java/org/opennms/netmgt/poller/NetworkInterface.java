/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

/**
 * <P>
 * The NetworkInterface class is designed to be a well defined front for passing
 * interfaces to a service monitor. There are many different types of network in
 * use today including IPv4, IPv6, IPX, and others. To accommodate the possible
 * differences this class provides the basic information that a monitor can use
 * to determine the type of interface and its expected address type.
 * </P>
 *
 * <P>
 * In addition to providing typing and address information, the interface allows
 * for the monitor to associate key-value pairs with an interface. This can be
 * used to save state information between the various invocations if necessary.
 * The attributes may be shared with other monitors concurrently, so a monitor
 * must be careful to choose unique keys to prevent namespace collisions.
 * </P>
 *
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public interface NetworkInterface<T> {
    /**
     * Defines an unknown interface that has no associated address information.
     */
    public static final int TYPE_UNKNOWN = 0;

    /**
     * <P>
     * Defines a standard IPv4 or IPv6 address. This is usually modeled by an
     * InetAddress object.
     * </P>
     */
    public static final int TYPE_INET = 1; // InetAddress object returned

    /**
     * <P>
     * Returns the interface type for the network interface.
     * </P>
     *
     * @return a int.
     */
    public int getType();

    /**
     * <P>
     * Returns the address information for the interface. If the interface then
     * this should be either an InetAddress or an object for specialized address
     * types.
     * </p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public T getAddress();

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
    public <V> V getAttribute(String property);

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
    public Object setAttribute(String property, Object value);
}
