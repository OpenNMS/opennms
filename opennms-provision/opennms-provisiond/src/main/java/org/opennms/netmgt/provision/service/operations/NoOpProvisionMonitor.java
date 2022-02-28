/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
