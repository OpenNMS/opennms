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

public class NoOpProvisionMonitor implements ProvisionMonitor {
    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void finish() {

    }

    @Override
    public int getNodeCount() {
        return 0;
    }

    @Override
    public void beginScheduling() {

    }

    @Override
    public void finishScheduling() {

    }

    @Override
    public void beginScanEvent(ImportOperation oper) {

    }

    @Override
    public void finishScanEvent(ImportOperation oper) {

    }

    @Override
    public void beginScanning(NodeScan nodeScan) {

    }

    @Override
    public void finishScanning(NodeScan nodeScan) {

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void beginPersisting(ImportOperation oper) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishPersisting(ImportOperation oper) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginSendingEvent(Event event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishSendingEvent(Event event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginLoadingResource(Resource resource) {
    }

    @Override
    public void finishLoadingResource(Resource resource, int nodeCount) {

    }

    /**
     * <p>beginImporting</p>
     */
    @Override
    public void beginImporting() {
    }

    /**
     * <p>finishImporting</p>
     */
    @Override
    public void finishImporting() {
    }

    /**
     * <p>beginAuditNodes</p>
     */
    @Override
    public void beginAuditNodes() {
    }

    /**
     * <p>finishAuditNodes</p>
     */
    @Override
    public void finishAuditNodes() {
    }

    /**
     * <p>beginRelateNodes</p>
     */
    @Override
    public void beginRelateNodes() {
    }

    /**
     * <p>finishRelateNodes</p>
     */
    @Override
    public void finishRelateNodes() {
    }

}
