/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.util.concurrent.ScheduledFuture;

/**
 * This class provides an API for implementing provider "extensions" to the OpenNMS
 * Provisioning daemon.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public interface ProvisioningAdapter {

    /**
     * This method is called by the Provisioner when a new node is provisioned.
     *
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     * @param nodeId a int.
     * @return 
     */
    ScheduledFuture<?> addNode(int nodeId) throws ProvisioningAdapterException;
    
    /**
     * This method is called by the Provisioner when a node is updated through provisioning.
     *
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     * @param nodeId a int.
     */
    ScheduledFuture<?> updateNode(int nodeId) throws ProvisioningAdapterException;
    
    /**
     * This method is called by the Provisioner when a node is deleted through provisioning.
     *
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     * @param nodeId a int.
     */
    ScheduledFuture<?> deleteNode(int nodeId) throws ProvisioningAdapterException;

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getName();

    /**
     * This method is called when a configuration change event has occurred from any source.  Typically,
     * Traps sent from a device are converted to an event and that event is then identified for translation
     * and translated into a generic configuration changed event.
     *
     * @param nodeid a int.
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     */
    ScheduledFuture<?> nodeConfigChanged(int nodeid) throws ProvisioningAdapterException;
    
    /**
     * This method is called in case the adapter needs to perform some initialization prior to
     * receiving calls from the AdapterManager.
     *
     * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
     */
    void init() throws ProvisioningAdapterException;
    
}
