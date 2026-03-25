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
package org.opennms.systemreport.event;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserLoginEventListener implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(UserLoginEventListener.class);

    private static final String CSV_CLEANUP_INITIAL_DELAY = "PT0S";
    private static final String CSV_CLEANUP_PERIOD = "P1D";

    private EventSubscriptionService eventSubscriptionService;
    private ScheduledExecutorService csvCleanupScheduler;

    @Override
    public void onEvent(IEvent event) {
        if (EventConstants.AUTHENTICATION_SUCCESS_UEI.equals(event.getUei())) {
            String username = event.getParm("user").getValue().getContent();
            if (!username.equals("rtc")) {
                CsvUtils.logUserDataToCsv(username, event.getTime());
            }
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public void init() {
        eventSubscriptionService.addEventListener(this,
                EventConstants.AUTHENTICATION_SUCCESS_UEI);

        csvCleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "UserLoginEventListener-CsvCleanup");
            t.setDaemon(true);
            return t;
        });
        final Duration initialDelay = Duration.parse(CSV_CLEANUP_INITIAL_DELAY);
        final Duration period = Duration.parse(CSV_CLEANUP_PERIOD);
        csvCleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                CsvUtils.removeOldRecordsFromCsv();
            } catch (Exception e) {
                LOG.warn("Failed to clean up old records from user login CSV", e);
            }
        }, initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void destroy() {
        eventSubscriptionService.removeEventListener(this);
        if (csvCleanupScheduler != null) {
            csvCleanupScheduler.shutdown();
        }
    }

    public EventSubscriptionService getEventSubscriptionService() {
        return eventSubscriptionService;
    }

    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

}
