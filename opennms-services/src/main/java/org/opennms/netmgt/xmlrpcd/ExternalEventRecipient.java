/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 13, 2005
 * 
 *
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
 */
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
