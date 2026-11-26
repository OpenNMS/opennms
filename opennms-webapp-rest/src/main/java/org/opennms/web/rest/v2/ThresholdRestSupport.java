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
package org.opennms.web.rest.v2;

import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;

/**
 * Helpers shared by the two threshold configuration REST services.
 */
abstract class ThresholdRestSupport {

    /** Matches the source string the JSP threshold editor used, so event history stays continuous. */
    private static final String EVENT_SOURCE = "Web UI";

    private ThresholdRestSupport() {
    }

    /** See {@link ConfigEtags#etagOf}. */
    static String etagOf(final ObjectMapper objectMapper, final Object dto) {
        return ConfigEtags.etagOf(objectMapper, dto);
    }

    /** See {@link ConfigEtags#matchesIfMatch}. */
    static boolean matchesIfMatch(final String ifMatch, final String etag) {
        return ConfigEtags.matchesIfMatch(ifMatch, etag);
    }

    /**
     * Tells the thresholding service that its configuration changed, so it rebuilds its thresholding sets.
     *
     * <p>Not {@code reloadDaemonConfig}: the save already reloaded the DAOs, which the webapp shares with the
     * daemons, through the configuration manager's update notification. A daemon reload would re-read both
     * documents from the database a second time, outside the lock the write held, before doing the one thing
     * still needed here.</p>
     */
    static void sendConfigChangedEvent(final EventProxy eventProxy) throws Exception {
        final EventBuilder builder = new EventBuilder(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI, EVENT_SOURCE);
        builder.setHost(InetAddressUtils.getLocalHostName());
        eventProxy.send(builder.getEvent());
    }
}
