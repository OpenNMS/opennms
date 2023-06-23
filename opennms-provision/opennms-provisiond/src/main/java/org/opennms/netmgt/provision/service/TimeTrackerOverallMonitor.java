/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.opennms.netmgt.provision.service.operations.ProvisionOverallMonitor;

/**
 * This class provides overall timers for the import lifeCycle: validate, audit, scan (schedule), delete update, insert, relate
 * and also a timer for overall single node scans.
 * These timers are associated to a single requisition
 * @see ./META-INF/opennms/applicationContext-provisiond
 */
public class TimeTrackerOverallMonitor implements ProvisionOverallMonitor {

    private Timer provisionOverall;
    private Timer provisionNodeScanOverall;
    private Timer.Context provisionDuration;
    private Timer.Context provisionNodeScanDuration;
    private String name;
    private MetricRegistry metricRegistry;

    public TimeTrackerOverallMonitor(final String name, final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.name = name;
        this.provisionOverall = metricRegistry.timer(name);
        this.provisionNodeScanOverall = metricRegistry.timer(MetricRegistry.name(name, "nodeScan"));
    }

    @Override
    public void start() {
        this.provisionDuration = this.provisionOverall.time();
    }

    @Override
    public void end(){
        this.provisionDuration.stop();
    }

    @Override
    public void startNodeScan() { this.provisionNodeScanDuration = this.provisionNodeScanOverall.time(); }

    @Override
    public void endNodeScan() { this.provisionNodeScanDuration.stop(); }

}
