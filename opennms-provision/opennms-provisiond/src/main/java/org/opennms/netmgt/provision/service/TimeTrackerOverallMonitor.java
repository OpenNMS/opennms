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
