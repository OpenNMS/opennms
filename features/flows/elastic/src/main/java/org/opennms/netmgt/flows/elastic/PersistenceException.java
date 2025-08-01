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
package org.opennms.netmgt.flows.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.api.DetailedFlowException;
import org.opennms.features.jest.client.bulk.FailedItem;

public class PersistenceException extends DetailedFlowException {

    private List<FailedItem<FlowDocument>> failedItems = new ArrayList<>();

    public PersistenceException(String message, List<FailedItem<FlowDocument>> failedItems) {
        super(message);
        this.failedItems = failedItems;
    }

    public List<FailedItem<FlowDocument>> getFailedItems() {
        return failedItems;
    }

    @Override
    public List<String> getDetailedLogMessages() {
        return getFailedItems().stream()
                .map(e -> String.format("Failed to persist item with convoKey '%s' and index %d: %s", e.getItem().getConvoKey(), e.getIndex(), e.getCause().getMessage())).collect(Collectors.toList());
    }
}
