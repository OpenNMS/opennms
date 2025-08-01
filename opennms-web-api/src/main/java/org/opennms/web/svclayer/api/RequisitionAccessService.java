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
package org.opennms.web.svclayer.api;

import javax.ws.rs.core.MultivaluedMap;

import org.opennms.netmgt.provision.persist.requisition.DeployedRequisitionStats;
import org.opennms.netmgt.provision.persist.requisition.DeployedStats;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAssetCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategoryCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterfaceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredServiceCollection;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNodeCollection;

public interface RequisitionAccessService {

	void flushAll();

	// GLOBAL
	int getDeployedCount();

	// GLOBAL
	RequisitionCollection getDeployedRequisitions();

	// GLOBAL
	RequisitionCollection getRequisitions();

	// GLOBAL
	int getPendingCount();

	// GLOBAL
	DeployedStats getDeployedStats();

	// GLOBAL
	DeployedRequisitionStats getDeployedStats(String foreignSource);

	Requisition getRequisition(String foreignSource);

	RequisitionNodeCollection getNodes(String foreignSource);

	RequisitionNode getNode(String foreignSource, String foreignId);

	RequisitionInterfaceCollection getInterfacesForNode(String foreignSource, String foreignId);

	RequisitionInterface getInterfaceForNode(String foreignSource, String foreignId, String ipAddress);

	RequisitionMonitoredServiceCollection getServicesForInterface(String foreignSource, String foreignId, String ipAddress);

	RequisitionMonitoredService getServiceForInterface(String foreignSource, String foreignId, String ipAddress, String service);

	RequisitionCategoryCollection getCategories(String foreignSource, String foreignId);

	RequisitionCategory getCategory(String foreignSource, String foreignId, String category);

	RequisitionAssetCollection getAssetParameters(String foreignSource, String foreignId);

	RequisitionAsset getAssetParameter(String foreignSource, String foreignId, String parameter);

	void addOrReplaceRequisition(Requisition requisition);

	void addOrReplaceNode(String foreignSource, RequisitionNode node);

	void addOrReplaceInterface(String foreignSource, String foreignId, RequisitionInterface iface);

	void addOrReplaceService(String foreignSource, String foreignId, String ipAddress, RequisitionMonitoredService service);

	void addOrReplaceNodeCategory(String foreignSource, String foreignId, RequisitionCategory category);

	void addOrReplaceNodeAssetParameter(String foreignSource, String foreignId, RequisitionAsset asset);

	void importRequisition(String foreignSource, String rescanExisting);

	void updateRequisition(String foreignSource, MultivaluedMap<String,String> params);

	void updateNode(String foreignSource, String foreignId, MultivaluedMap<String,String> params);

	void updateInterface(String foreignSource, String foreignId, String ipAddress, MultivaluedMap<String,String> params);

	void deletePendingRequisition(String foreignSource);

	void deleteDeployedRequisition(String foreignSource);

	void deleteNode(String foreignSource, String foreignId);

	void deleteInterface(String foreignSource, String foreignId, String ipAddress);

	void deleteInterfaceService(String foreignSource, String foreignId, String ipAddress, String service);

	void deleteCategory(String foreignSource, String foreignId, String category);

	void deleteAssetParameter(String foreignSource, String foreignId, String parameter);

}