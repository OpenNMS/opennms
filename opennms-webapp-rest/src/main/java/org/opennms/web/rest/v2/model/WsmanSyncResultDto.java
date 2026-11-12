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

/** What a sync of one server definition into its requisition did. */
public class WsmanSyncResultDto {
    private final String requisition;
    private final List<String> addedNodes = new ArrayList<>();
    private int existingNodes;
    private final List<String> addedRanges = new ArrayList<>();
    private int existingRanges;
    private final List<String> skippedPatterns = new ArrayList<>();
    private boolean importRequested;
    private boolean discoveryReloadRequested;

    public WsmanSyncResultDto(final String requisition) {
        this.requisition = requisition;
    }

    public String getRequisition() { return requisition; }
    public List<String> getAddedNodes() { return addedNodes; }
    public int getExistingNodes() { return existingNodes; }
    public void countExistingNode() { existingNodes++; }
    public List<String> getAddedRanges() { return addedRanges; }
    public int getExistingRanges() { return existingRanges; }
    public void countExistingRange() { existingRanges++; }
    public List<String> getSkippedPatterns() { return skippedPatterns; }
    public boolean isImportRequested() { return importRequested; }
    public void setImportRequested(final boolean importRequested) { this.importRequested = importRequested; }
    public boolean isDiscoveryReloadRequested() { return discoveryReloadRequested; }
    public void setDiscoveryReloadRequested(final boolean discoveryReloadRequested) { this.discoveryReloadRequested = discoveryReloadRequested; }
}
