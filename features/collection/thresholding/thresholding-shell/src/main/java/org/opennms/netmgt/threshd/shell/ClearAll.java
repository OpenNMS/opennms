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
package org.opennms.netmgt.threshd.shell;

import java.util.concurrent.CompletableFuture;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.threshd.api.ThresholdStateMonitor;

@Command(scope = "opennms", name = "threshold-clear-all", description = "Clears all threshold states")
@Service
public class ClearAll extends AbstractThresholdStateCommand {
    @Reference
    ThresholdStateMonitor thresholdStateMonitor;

    @Option(name = "-p", aliases = "--persisted-only", description = "When set, clears only the persisted state")
    private boolean clearPersistedOnly;

    @Override
    public Object execute() throws InterruptedException {
        System.out.print("Clearing all thresholding states...");

        CompletableFuture<Void> clearFuture;

        if (clearPersistedOnly) {
            clearFuture = blobStore.truncateContextAsync(THRESHOLDING_KV_CONTEXT);
        } else {
            clearFuture = CompletableFuture.supplyAsync(() -> {
                thresholdStateMonitor.reinitializeStates();
                return null;
            });
        }

        while (!clearFuture.isDone()) {
            Thread.sleep(1000);
            System.out.print('.');
        }

        try {
            clearFuture.get();
            System.out.println("done");
        } catch (Exception e) {
            System.out.println("Failed to clear all states" + e);
        }

        return null;
    }
}