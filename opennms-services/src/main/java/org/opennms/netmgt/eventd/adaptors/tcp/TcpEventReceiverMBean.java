//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd.adaptors.tcp;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>TcpEventReceiverMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface TcpEventReceiverMBean extends BaseOnmsMBean{
    /**
     * Invoked prior to garbage collection.
     */
    void destroy();

    /**
     * Sets the port where new requests will be handled. This can only be done
     * prior to starting the managed bean. If the managed bean is already
     * running then an exception is thrown.
     *
     * @param port
     *            The port to listen on.
     */
    void setPort(Integer port);

    /**
     * Returns the where a listener is waiting to process new request.
     *
     * @return The listening port.
     */
    Integer getPort();

    /**
     * Adds a new event handler by its managed name.
     *
     * @param name
     *            The name of the handler to add.
     * @throws javax.management.MalformedObjectNameException
     *             Thrown if the passed name is not a valid ObjectName.
     * @throws javax.management.InstanceNotFoundException
     *             Thrown if no managed bean can be found that matches the name.
     */
    void addEventHandler(String name) throws MalformedObjectNameException, InstanceNotFoundException;

    /**
     * Removes an event handler. The passed name must be a valid JMX object
     * name.
     *
     * @param name
     *            The name of the handler to remove.
     * @throws javax.management.MalformedObjectNameException
     *             Thrown if the passed name is not a valid ObjectName.
     * @throws javax.management.InstanceNotFoundException
     *             Thrown if no managed bean can be found that matches the name.
     */
    void removeEventHandler(String name) throws MalformedObjectNameException, InstanceNotFoundException;

    /**
     * The logging prefix to use
     *
     * @param prefix a {@link java.lang.String} object.
     */
    void setLogPrefix(String prefix);

    /**
     * The number of event records a new connection is allowed to send before
     * the connection is terminated by the server. The connection is always
     * terminated after an event receipt is generated, if one is required.
     *
     * @param number
     *            The number of event records.
     */
    void setEventsPerConnection(Integer number);
}
