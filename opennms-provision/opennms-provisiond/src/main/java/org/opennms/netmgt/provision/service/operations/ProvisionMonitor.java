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

import org.opennms.netmgt.provision.service.NodeScan;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

public interface ProvisionMonitor {

	/**
	 * @return name of the monitor (also act as key in MonitorHolder)
	 */
	String getName();

	/**
	 * capture start time of the monitor
	 */
	void start();

	/**
	 * capture finish time of the monitor
	 */
	void finish();

	/**
	 * @return total number of nodes in resources
	 */
	int getNodeCount();
	/**
	 * <p>beginScheduling</p>
	 */
	void beginScheduling();

	/**
	 * <p>finishScheduling</p>
	 */
	void finishScheduling();

	/**
	 * <p>beginPreprocessing</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
	 */
	void beginScanEvent(ImportOperation oper);

	/**
	 * <p>finishPreprocessing</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
	 */
	void finishScanEvent(ImportOperation oper);

	/**
	 * <p>beginPreprocessing</p>
	 *
	 * @param nodeScan a {@link org.opennms.netmgt.provision.service.NodeScan} object.
	 */
	void beginScanning(NodeScan nodeScan);

	/**
	 * <p>finishPreprocessing</p>
	 *
	 * @param nodeScan a {@link org.opennms.netmgt.provision.service.NodeScan} object.
	 */
	void finishScanning(NodeScan nodeScan);

	/**
	 * <p>beginPersisting</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
	 */
	void beginPersisting(ImportOperation oper);

	/**
	 * <p>finishPersisting</p>
	 *
	 * @param oper a {@link org.opennms.netmgt.provision.service.operations.ImportOperation} object.
	 */
	void finishPersisting(ImportOperation oper);

	/**
	 * <p>beginSendingEvents</p>
	 *
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	void beginSendingEvent(Event event);

	/**
	 * <p>finishSendingEvents</p>
	 *
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	void finishSendingEvent(Event event);

	/**
	 * <p>beginLoadingResource</p>
	 *
	 * @param resource a {@link org.springframework.core.io.Resource} object.
	 */
	void beginLoadingResource(Resource resource);

	/**
	 * <p>finishLoadingResource</p>
	 *
	 * @param resource a {@link org.springframework.core.io.Resource} object.
	 */
	void finishLoadingResource(Resource resource, int nodeCount);

	/**
	 * <p>beginImporting</p>
	 */
	void beginImporting();

	/**
	 * <p>finishImporting</p>
	 */
	void finishImporting();

	/**
	 * <p>beginAuditNodes</p>
	 */
	void beginAuditNodes();

	/**
	 * <p>finishAuditNodes</p>
	 */
	void finishAuditNodes();

	/**
	 * <p>beginRelateNodes</p>
	 */
	void beginRelateNodes();

	/**
	 * <p>finishRelateNodes</p>
	 */
	void finishRelateNodes();
}
