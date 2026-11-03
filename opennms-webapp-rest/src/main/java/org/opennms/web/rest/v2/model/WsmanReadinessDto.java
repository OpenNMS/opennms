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
package org.opennms.web.rest.v2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Whether the rest of OpenNMS is set up so that WS-Man servers are actually
 * polled and collected: the poller and collectd must include the WS-Man
 * service, and servers provisioned before that was true stay unpolled until
 * their requisition is rescanned.
 */
public class WsmanReadinessDto {
    private boolean pollerService;
    private boolean pollerMonitor;
    private String pollerPackage;
    private boolean collectdService;
    private boolean collectdCollector;
    private int servers;
    private int polledServers;
    private int unpolledServers;
    private final List<String> requisitionsWithUnpolled = new ArrayList<>();

    public boolean isReady() { return pollerService && pollerMonitor && collectdService && collectdCollector; }
    public boolean isPollerService() { return pollerService; }
    public void setPollerService(boolean v) { pollerService = v; }
    public boolean isPollerMonitor() { return pollerMonitor; }
    public void setPollerMonitor(boolean v) { pollerMonitor = v; }
    public String getPollerPackage() { return pollerPackage; }
    public void setPollerPackage(String v) { pollerPackage = v; }
    public boolean isCollectdService() { return collectdService; }
    public void setCollectdService(boolean v) { collectdService = v; }
    public boolean isCollectdCollector() { return collectdCollector; }
    public void setCollectdCollector(boolean v) { collectdCollector = v; }
    public int getServers() { return servers; }
    public void setServers(int v) { servers = v; }
    public int getPolledServers() { return polledServers; }
    public void setPolledServers(int v) { polledServers = v; }
    public int getUnpolledServers() { return unpolledServers; }
    public void setUnpolledServers(int v) { unpolledServers = v; }
    public List<String> getRequisitionsWithUnpolled() { return requisitionsWithUnpolled; }
}
