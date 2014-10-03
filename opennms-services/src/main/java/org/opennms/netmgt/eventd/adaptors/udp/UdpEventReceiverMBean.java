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

package org.opennms.netmgt.eventd.adaptors.udp;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>UdpEventReceiverMBean interface.</p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 * @version $Id: $
 */
public interface UdpEventReceiverMBean extends BaseOnmsMBean {
    /**
     * <p>destroy</p>
     */
    void destroy();

    /**
     * <p>setPort</p>
     *
     * @param port a {@link java.lang.Integer} object.
     */
    void setPort(Integer port);

    /**
     * <p>getPort</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    Integer getPort();

    /**
     * <p>addEventHandler</p>
     *
     * @param name a {@link java.lang.String} object.
     * @throws javax.management.MalformedObjectNameException if any.
     * @throws javax.management.InstanceNotFoundException if any.
     */
    void addEventHandler(String name) throws MalformedObjectNameException, InstanceNotFoundException;

    /**
     * <p>removeEventHandler</p>
     *
     * @param name a {@link java.lang.String} object.
     * @throws javax.management.MalformedObjectNameException if any.
     * @throws javax.management.InstanceNotFoundException if any.
     */
    void removeEventHandler(String name) throws MalformedObjectNameException, InstanceNotFoundException;

    /**
     * <p>setLogPrefix</p>
     *
     * @param prefix a {@link java.lang.String} object.
     */
    void setLogPrefix(String prefix);
}
