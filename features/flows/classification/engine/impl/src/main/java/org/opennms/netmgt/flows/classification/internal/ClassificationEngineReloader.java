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
package org.opennms.netmgt.flows.classification.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassificationEngineReloader {

    private static final Logger LOG = LoggerFactory.getLogger(ClassificationEngineReloader.class);

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public ClassificationEngineReloader(Identity identity, ClassificationEngine engine, String reloadIntervalString){
        if (identity != null) {
            final int reloadInterval = Integer.parseInt(reloadIntervalString);
            LOG.debug("Scheduling reload of classification engine every {} seconds", reloadInterval);
            executorService.scheduleWithFixedDelay(() -> {
                LOG.debug("Performing reload of Classification Engine...");
                try {
                    engine.reload();
                } catch (InterruptedException e) {
                    LOG.error("reload was interrupted", e);
                }
                LOG.debug("Reload of Classification Engine performed. Next reload will be in {} seconds", reloadInterval);
            }, reloadInterval, reloadInterval, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        LOG.debug("Shutting down {}", getClass().getSimpleName());
        executorService.shutdown();
    }
}
