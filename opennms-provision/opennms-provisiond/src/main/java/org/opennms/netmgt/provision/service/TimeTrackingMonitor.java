/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.provision.service.operations.SaveOrUpdateOperation;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>TimeTrackingMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class TimeTrackingMonitor implements ProvisionMonitor {
    private final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    private WorkDuration importDuration;
    private WorkDuration auditDuration;
    private WorkDuration loadingDuration;
    private WorkDuration processingDuration;
    private WorkDuration preprocessingDuration;
    private WorkDuration relateDuration;
    private WorkEffort scanningEffort;
    private WorkEffort processingEffort;
    private WorkEffort eventEffort;
    private int eventCount;
    private int nodeCount;
    private Map<String, Date> currentNodes;
    private Date startTime;
    private Date endTime;



    public TimeTrackingMonitor() {
        reset();
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    @Override
    public void start() {
        // make sure all old data is removed
        reset();
        startTime = new Date();
    }

    @Override
    public void end() {
        endTime = new Date();
    }

    public void reset() {
        importDuration = new WorkDuration("Importing");
        auditDuration = new WorkDuration("Auditing");
        loadingDuration = new WorkDuration("Loading");
        processingDuration = new WorkDuration("Processing");
        preprocessingDuration = new WorkDuration("Scanning");
        relateDuration = new WorkDuration("Relating");
        scanningEffort = new WorkEffort("Scan Effort");
        processingEffort = new WorkEffort("Write Effort");
        eventEffort = new WorkEffort("Event Sending Effort");
        eventCount = 0;
        nodeCount = 0;
        currentNodes = new HashMap<>();
    }

    public Map<String, Date> getCurrentNodes() {
        return currentNodes;
    }

    public WorkDuration getImportDuration() {
        return importDuration;
    }

    public WorkDuration getAuditDuration() {
        return auditDuration;
    }

    public WorkDuration getLoadingDuration() {
        return loadingDuration;
    }

    public WorkDuration getProcessingDuration() {
        return processingDuration;
    }

    public WorkDuration getPreprocessingDuration() {
        return preprocessingDuration;
    }

    public WorkDuration getRelateDuration() {
        return relateDuration;
    }

    public WorkEffort getScanningEffort() {
        return scanningEffort;
    }

    public WorkEffort getProcessingEffort() {
        return processingEffort;
    }

    public WorkEffort getEventEffort() {
        return eventEffort;
    }

    public int getEventCount() {
        return eventCount;
    }

    @Override
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * <p>beginPreprocessingOps</p>
     */
    @Override
    public void beginPreprocessingOps() {
        preprocessingDuration.start();
    }

    /**
     * <p>finishPreprocessingOps</p>
     */
    @Override
    public void finishPreprocessingOps() {
        preprocessingDuration.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginScanning(ImportOperation oper) {
        if (oper instanceof SaveOrUpdateOperation) {
            currentNodes.put(oper.toString(), new Date());
        }
        scanningEffort.begin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishScanning(ImportOperation oper) {
        scanningEffort.end();
        if (oper instanceof SaveOrUpdateOperation) {
            currentNodes.remove(oper.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginPersisting(ImportOperation oper) {
        processingEffort.begin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishPersisting(ImportOperation oper) {
        processingEffort.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginSendingEvent(Event event) {
        if (event != null) {
            eventCount++;
        }
        eventEffort.begin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishSendingEvent(Event event) {
        eventEffort.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginLoadingResource(Resource resource) {
        loadingDuration.setName("Loading Resource: " + resource);
        loadingDuration.start();
    }

    @Override
    public void finishLoadingResource(Resource resource, int nodeCount) {
        loadingDuration.end();
        this.nodeCount = nodeCount;
    }

    /**
     * <p>beginImporting</p>
     */
    @Override
    public void beginImporting() {
        importDuration.start();
    }

    /**
     * <p>finishImporting</p>
     */
    @Override
    public void finishImporting() {
        importDuration.end();
    }

    /**
     * <p>beginAuditNodes</p>
     */
    @Override
    public void beginAuditNodes() {
        auditDuration.start();
    }

    /**
     * <p>finishAuditNodes</p>
     */
    @Override
    public void finishAuditNodes() {
        auditDuration.end();
    }

    /**
     * <p>beginRelateNodes</p>
     */
    @Override
    public void beginRelateNodes() {
        relateDuration.start();
    }

    /**
     * <p>finishRelateNodes</p>
     */
    @Override
    public void finishRelateNodes() {
        relateDuration.end();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder stats = new StringBuilder();
        stats.append("NodeCount: ").append(nodeCount).append("\n");
        stats.append(importDuration).append(", ");
        stats.append(loadingDuration).append(", ");
        stats.append(auditDuration).append('\n');
        stats.append(preprocessingDuration).append(", ");
        stats.append(processingDuration).append(", ");
        stats.append(relateDuration).append("\n");
        stats.append(scanningEffort).append(", ");
        stats.append(processingEffort).append(", ");
        stats.append(eventEffort);
        if (eventCount > 0) {
            stats.append(", Avg ").append((double) eventEffort.getTotalTime() / (double) eventCount).append(" ms per event");
        }

        return stats.toString();
    }

}
