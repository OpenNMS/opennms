/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

/*
 * FIXME: seems like the method: isServiceDetected should be defined here.
 */
/**
 * ServiceDetector
 *
 * @author <a href="mailto:brozow@opennms.org>Mathew Brozowski</a>
 */
public interface ServiceDetector {
    
    /*
     * FIXME: Document this API requirement.  Not sure what is expected, perhaps nothing
     * but there is a lot of inconsistency in the implementation from abstract classes and their
     * implementations.
     */
    /**
     * Perform any necessary initialization after construction and before detecting.
     */
    public void init();
    
    /*
     * FIXME: Probably should make sure that the service names are always unique.
     */
    
    /**
     * Requires that all implementations of this API return a service name.
     * @return
     */
    public String getServiceName();

    /**
     * Service name is mutable so that we can create new instances of each implementation
     * and define a new service detector using the underlying protocol.
     * 
     * @param serviceName
     */
    public void setServiceName(String serviceName);

    
    /**
     * The detector should clean up after itself in this method if necessary.
     */
    public void dispose();

}
