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
package org.opennms.features.datachoices.web.internal;

import java.util.stream.Stream;

import org.opennms.features.datachoices.internal.StateManager;
import org.opennms.web.api.WizardGateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mirrors the gate logic in ModalInjector but for the Vue wizard redirect path.
 * Registered as an OSGi WizardGateService so the auth success handler can look
 * it up without a compile-time dependency on the datachoices module.
 */
public class WizardGateServiceImpl implements WizardGateService {
    private static final Logger LOG = LoggerFactory.getLogger(WizardGateServiceImpl.class);

    private final StateManager stateManager;

    public WizardGateServiceImpl(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    @Override
    public boolean hasUnacknowledgedNotices(boolean isAdmin, boolean isAdminOrRest) {
        try {
            if (isAdmin && needsUsageStatisticsNotice()) {
                return true;
            }
            if (isAdminOrRest && isProductEnrollmentEnabled() && needsProductEnrollmentNotice()) {
                return true;
            }
        } catch (Exception e) {
            LOG.warn("Error checking wizard gate status; skipping notice redirect.", e);
        }
        return false;
    }

    private boolean needsUsageStatisticsNotice() throws Exception {
        Boolean acked = stateManager.isInitialNoticeAcknowledged();
        Boolean enabled = stateManager.isEnabled();
        // Already acked OR already explicitly opted out → nothing to show
        boolean alreadyAcked = Boolean.TRUE.equals(acked);
        boolean alreadyOptedOut = enabled != null && !enabled;
        return !alreadyAcked && !alreadyOptedOut;
    }

    private boolean needsProductEnrollmentNotice() throws Exception {
        Boolean noticed = stateManager.isProductUpdateEnrollmentNoticeAcknowledged();
        return !Boolean.TRUE.equals(noticed);
    }

    private static boolean isProductEnrollmentEnabled() {
        return Stream.of("opennms.productUpdateEnrollment.show", "opennms.userDataCollection.show")
                .map(key -> System.getProperty(key, "true"))
                .noneMatch(val -> val.equalsIgnoreCase("false"));
    }
}
