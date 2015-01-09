/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xmlrpcd;

/**
 * <p>ExternalEventRecipient interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface ExternalEventRecipient {

    /**
     * <p>notifySuccess</p>
     *
     * @param txNo a long.
     * @param uei a {@link java.lang.String} object.
     * @param message a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object notifySuccess(long txNo, String uei, String message);

    /**
     * <p>notifyFailure</p>
     *
     * @param txNo a long.
     * @param uei a {@link java.lang.String} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object notifyFailure(long txNo, String uei, String reason);

    /**
     * <p>notifyReceivedEvent</p>
     *
     * @param txNo a long.
     * @param uei a {@link java.lang.String} object.
     * @param message a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object notifyReceivedEvent(long txNo, String uei, String message);

    /**
     * <p>sendServiceDownEvent</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param iface a {@link java.lang.String} object.
     * @param service a {@link java.lang.String} object.
     * @param msg a {@link java.lang.String} object.
     * @param host a {@link java.lang.String} object.
     * @param time a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object sendServiceDownEvent(String nodeLabel, String iface, String service, String msg, String host, String time);

    /**
     * <p>sendServiceUpEvent</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param interface1 a {@link java.lang.String} object.
     * @param service a {@link java.lang.String} object.
     * @param msg a {@link java.lang.String} object.
     * @param eventHost a {@link java.lang.String} object.
     * @param time a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object sendServiceUpEvent(String nodeLabel, String interface1, String service, String msg, String eventHost, String time);

    /**
     * <p>sendInterfaceDownEvent</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param interface1 a {@link java.lang.String} object.
     * @param eventHost a {@link java.lang.String} object.
     * @param time a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object sendInterfaceDownEvent(String nodeLabel, String interface1, String eventHost, String time);

    /**
     * <p>sendInterfaceUpEvent</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param interface1 a {@link java.lang.String} object.
     * @param eventHost a {@link java.lang.String} object.
     * @param time a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object sendInterfaceUpEvent(String nodeLabel, String interface1, String eventHost, String time);

    /**
     * <p>sendNodeDownEvent</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param eventHost a {@link java.lang.String} object.
     * @param time a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object sendNodeDownEvent(String nodeLabel, String eventHost, String time);

    /**
     * <p>sendNodeUpEvent</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param eventHost a {@link java.lang.String} object.
     * @param time a {@link java.lang.String} object.
     * @return a {@link java.lang.Object} object.
     */
    Object sendNodeUpEvent(String nodeLabel, String eventHost, String time);


}
