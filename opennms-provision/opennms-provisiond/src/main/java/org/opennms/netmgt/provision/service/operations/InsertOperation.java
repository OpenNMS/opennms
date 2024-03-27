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

public class InsertOperation extends SaveOrUpdateOperation {
    
    /**
     * <p>Constructor for InsertOperation.</p>
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param location a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     * @param monitorKey a {@link java.lang.String} object. (optional)
     */
    public InsertOperation(String foreignSource, String foreignId, String nodeLabel, String location, String building, String city, ProvisionService provisionService, String monitorKey) {
        super(null, foreignSource, foreignId, nodeLabel, location, building, city, provisionService, Boolean.TRUE.toString(), monitorKey);
    }

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
    @Override
	public String toString() {
        return "INSERT: Node: "+(getNode().getId() == null ? "[no ID]" : getNode().getId())+": "+getNode().getLabel();
    }

    /** {@inheritDoc} */
    @Override
    protected void doPersist() {
        getProvisionService().insertNode(getNode(), getMonitorKey());
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.INSERT;
    }


}
