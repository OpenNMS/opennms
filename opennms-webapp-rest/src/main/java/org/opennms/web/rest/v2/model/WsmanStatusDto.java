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
import java.util.Date;
import java.util.List;

/**
 * What the poller currently sees for the servers each WS-Man server
 * definition matches: how many managed services carry the WS-Man service,
 * how many of them have no open outage, and when one last responded.
 */
public class WsmanStatusDto {

    public static class Bucket {
        private int servers;
        private int responding;
        private int down;
        // services provisioning marked as not polled: no poller package covers WS-Man for them
        private int unpolled;
        private Date lastResponse;

        public int getServers() { return servers; }
        public int getResponding() { return responding; }
        public int getDown() { return down; }
        public int getUnpolled() { return unpolled; }
        public void countUnpolled() { unpolled++; }
        public Date getLastResponse() { return lastResponse; }

        public void count(final boolean isDown, final Date lastGood) {
            servers++;
            if (isDown) {
                down++;
            } else {
                responding++;
            }
            if (lastGood != null && (lastResponse == null || lastGood.after(lastResponse))) {
                lastResponse = lastGood;
            }
        }
    }

    public static class DefinitionStatus extends Bucket {
        private final int index;
        private String requisition;
        // of the definition's specific addresses, how many exist as a node interface
        private int specificAddresses;
        private int provisioned;

        public DefinitionStatus(final int index) {
            this.index = index;
        }

        public int getIndex() { return index; }
        public String getRequisition() { return requisition; }
        public void setRequisition(final String requisition) { this.requisition = requisition; }
        public int getSpecificAddresses() { return specificAddresses; }
        public int getProvisioned() { return provisioned; }
        public void countSpecific(final boolean isProvisioned) {
            specificAddresses++;
            if (isProvisioned) {
                provisioned++;
            }
        }
    }

    private final String serviceName;
    private final List<DefinitionStatus> definitions = new ArrayList<>();
    private final Bucket defaults = new Bucket();

    public WsmanStatusDto(final String serviceName, final int definitionCount) {
        this.serviceName = serviceName;
        for (int i = 0; i < definitionCount; i++) {
            definitions.add(new DefinitionStatus(i));
        }
    }

    public String getServiceName() { return serviceName; }
    public List<DefinitionStatus> getDefinitions() { return definitions; }
    public Bucket getDefaults() { return defaults; }
    public int getServers() { return definitions.stream().mapToInt(Bucket::getServers).sum() + defaults.getServers(); }
}
