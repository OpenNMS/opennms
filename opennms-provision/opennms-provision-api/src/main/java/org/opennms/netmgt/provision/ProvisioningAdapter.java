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
