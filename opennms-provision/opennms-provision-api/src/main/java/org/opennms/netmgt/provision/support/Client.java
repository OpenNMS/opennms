/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.provision.support;

import java.io.IOException;
import java.net.InetAddress;

/**
 * <p>Client interface.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public interface Client<Request, Response> {
    
    /**
     * <p>connect</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param port a int.
     * @param timeout a int.
     * @param <Request> a Request object.
     * @param <Response> a Response object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception;
    
    /**
     * <p>receiveBanner</p>
     *
     * @return a Response object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    Response receiveBanner() throws IOException, Exception;
    
    /**
     * <p>sendRequest</p>
     *
     * @param request a Request object.
     * @return a Response object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    Response sendRequest(Request request) throws IOException, Exception; 
    
    /**
     * <p>close</p>
     */
    public void close();

}
