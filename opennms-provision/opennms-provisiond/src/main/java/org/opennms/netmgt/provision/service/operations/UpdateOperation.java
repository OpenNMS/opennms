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
package org.opennms.netmgt.provision.service.operations;

import org.opennms.netmgt.provision.service.ProvisionService;

public class UpdateOperation extends SaveOrUpdateOperation {
    
    /**
     * <p>Constructor for UpdateOperation.</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param location a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @param rescanExisting a {@link java.lang.String} object
     * @param monitorKey a {@link java.lang.String} object (optional)
     */
    public UpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String location, String building, String city, ProvisionService provisionService, String rescanExisting, String monitorKey) {
        super(nodeId, foreignSource, foreignId, nodeLabel, location, building, city, provisionService, rescanExisting, monitorKey);
    }

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@Override
	public String toString() {
       return "UPDATE: Node: "+(getNode().getId() == null ? "[no ID]" : getNode().getId())+": "+getNode().getLabel();
    }

	/** {@inheritDoc} */
	@Override
    protected void doPersist() {
        getProvisionService().updateNode(getNode(), getRescanExisting(), getMonitorKey());
    }
}
