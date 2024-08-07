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
package org.opennms.netmgt.provision.service;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import org.opennms.netmgt.provision.service.operations.ImportOperation;
import org.opennms.netmgt.provision.service.operations.ProvisionMonitor;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.Resource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>TimeTrackingMonitor class. It will append all data into MetricRegistry.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class TimeTrackingMonitor implements ProvisionMonitor {
    private MetricRegistry metricRegistry;

    private Timer loadingTimer;
    private Timer auditTimer;
    private Timer importTimer;
    private Timer schedulingTimer;
    private Timer relateTimer;

    private Context importDuration;
    private Context auditDuration;
    private Context loadingDuration;
    private Context schedulingDuration;
    private Context relateDuration;

    private ObjectKeyTimer scanEventTimer;
    private ObjectKeyTimer scanningTimer;

    private ObjectKeyTimer persistingTimer;
    private ObjectKeyTimer eventTimer;

    // total node count in resources
    private int nodeCount;

    // name of the monitor
    private String name;

    private Date startTime;
    private Date endTime;

    // current scanning node
    private Map<NodeScan, Date> currentNodes = new HashMap<>();

    public TimeTrackingMonitor(String name, MetricRegistry metricRegistry) {
        this.name = name;
        this.metricRegistry = metricRegistry;
        this.scanEventTimer = new ObjectKeyTimer(metricRegistry.timer(MetricRegistry.name(name, "Scan Event")));
        this.persistingTimer = new ObjectKeyTimer(metricRegistry.timer(MetricRegistry.name(name, "Persisting")));
        this.eventTimer = new ObjectKeyTimer(metricRegistry.timer(MetricRegistry.name(name, "Event")));
        this.scanningTimer = new ObjectKeyTimer(metricRegistry.timer(MetricRegistry.name(name, "Scanning")));
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        startTime = new Date();
    }

    @Override
    public void finish() {
        endTime = new Date();
    }

    public Map<NodeScan, Date> getCurrentNodes() {
        return currentNodes;
    }

    public Timer getLoadingTimer() {
        return loadingTimer;
    }

    public Timer getAuditTimer() {
        return auditTimer;
    }

    public Timer getImportTimer() {
        return importTimer;
    }

    public Timer getSchedulingTimer() {
        return schedulingTimer;
    }

    public Timer getRelateTimer() {
        return relateTimer;
    }

    public Timer getScanEventTimer() {
        return scanEventTimer.getTimer();
    }

    public Timer getScanningTimer() {
        return scanningTimer.getTimer();
    }

    public Timer getPersistingTimer() {
        return persistingTimer.getTimer();
    }

    public Timer getEventTimer() {
        return eventTimer.getTimer();
    }

    @Override
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * <p>beginScheduling</p>
     */
    @Override
    public void beginScheduling() {
        schedulingTimer = metricRegistry.timer(MetricRegistry.name(name, "Scheduling"));
        schedulingDuration = schedulingTimer.time();
    }

    /**
     * <p>finishScheduling</p>
     */
    @Override
    public void finishScheduling() {
        if (schedulingDuration != null) {
            schedulingDuration.stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginScanEvent(ImportOperation oper) {
        scanEventTimer.begin(oper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishScanEvent(ImportOperation oper) {
        scanEventTimer.end(oper);

    }

    @Override
    public void beginScanning(NodeScan nodeScan) {
        synchronized (currentNodes) {
            currentNodes.put(nodeScan, new Date());
        }
        scanningTimer.begin(nodeScan);
    }

    @Override
    public void finishScanning(NodeScan nodeScan) {
        scanningTimer.end(nodeScan);
        synchronized (currentNodes) {
            currentNodes.remove(nodeScan);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginPersisting(ImportOperation oper) {
        persistingTimer.begin(oper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishPersisting(ImportOperation oper) {
        persistingTimer.end(oper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginSendingEvent(Event event) {
        eventTimer.begin(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishSendingEvent(Event event) {
        eventTimer.end(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beginLoadingResource(Resource resource) {
        loadingTimer = metricRegistry.timer(MetricRegistry.name(name, "Loading", resource.getFilename()));
        loadingDuration = loadingTimer.time();
    }

    @Override
    public void finishLoadingResource(Resource resource, int nodeCount) {
        if (loadingDuration != null) {
            loadingDuration.stop();
        }
        this.nodeCount = nodeCount;
    }

    /**
     * <p>beginImporting</p>
     */
    @Override
    public void beginImporting() {
        importTimer = metricRegistry.timer(MetricRegistry.name(name, "Importing"));
        importDuration = importTimer.time();
    }

    /**
     * <p>finishImporting</p>
     */
    @Override
    public void finishImporting() {
        if (importDuration != null) {
            importDuration.stop();
        }
    }

    /**
     * <p>beginAuditNodes</p>
     */
    @Override
    public void beginAuditNodes() {
        auditTimer = metricRegistry.timer(MetricRegistry.name(name, "Auditing"));
        auditDuration = auditTimer.time();
    }

    /**
     * <p>finishAuditNodes</p>
     */
    @Override
    public void finishAuditNodes() {
        if (auditDuration != null) {
            auditDuration.stop();
        }
    }

    /**
     * <p>beginRelateNodes</p>
     */
    @Override
    public void beginRelateNodes() {
        relateTimer = metricRegistry.timer(MetricRegistry.name(name, "Relating"));
        relateDuration = relateTimer.time();
    }

    /**
     * <p>finishRelateNodes</p>
     */
    @Override
    public void finishRelateNodes() {
        if (relateDuration != null) {
            relateDuration.stop();
        }
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
        stats.append(schedulingDuration).append(", ");
        stats.append(relateDuration).append("\n");
        stats.append(scanEventTimer.getTimer().getMeanRate()).append(", ");
        stats.append(persistingTimer.getTimer().getMeanRate()).append(", ");
        stats.append(eventTimer.getTimer().getMeanRate());

        return stats.toString();
    }
}
