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

public class DeleteOperation extends ImportOperation {
    
    private Integer m_nodeId;
    
    /**
     * <p>Constructor for DeleteOperation.</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     */
    public DeleteOperation(Integer nodeId, String foreignSource, String foreignId, ProvisionService provisionService) {
        super(provisionService);
        m_nodeId = nodeId;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
    	return "DELETE: Node "+m_nodeId;
    }

	/**
	 * <p>scan</p>
	 */
	@Override
	public void scan() {
		// no additional data to gather
	}

	/** {@inheritDoc} */
	@Override
    protected void doPersist() {
        getProvisionService().deleteNode(m_nodeId);
    }

    @Override
    public OperationType getOperationType() {
        return OperationType.DELETE;
    }

}
