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
package org.opennms.web.rest.v2.plugins;
import java.util.List;

import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

/** The events the page raises when a plugin changes state; their definitions live in the opennms.plugin-management.events source. */
final class PluginEvents {
    static final String SOURCE = "PluginManagement";
    static final String UEI_PREFIX = "uei.opennms.org/internal/plugins/";
    static final String PLUGIN_STARTED_UEI = UEI_PREFIX + "pluginStarted";
    static final String PLUGIN_FAILED_UEI = UEI_PREFIX + "pluginFailed";
    static final String PLUGIN_STOPPED_UEI = UEI_PREFIX + "pluginStopped";
    static final String PARM_KAR_NAME = "karName";
    static final String PARM_FEATURES = "features";
    static final String PARM_REASON = "reason";
    static final String PARM_USER = "user";
    static final String PARM_SOURCE = "source";

    private PluginEvents() {
    }

    static Event started(final String karName, final List<String> features, final String user, final String source) {
        return build(PLUGIN_STARTED_UEI, karName, features, null, user, source);
    }

    static Event failed(final String karName, final List<String> features, final String reason, final String user, final String source) {
        return build(PLUGIN_FAILED_UEI, karName, features, reason == null ? "unknown" : reason, user, source);
    }

    static Event stopped(final String karName, final List<String> features, final String user, final String source) {
        return build(PLUGIN_STOPPED_UEI, karName, features, null, user, source);
    }

    private static Event build(final String uei, final String karName, final List<String> features, final String reason, final String user, final String source) {
        final EventBuilder builder = new EventBuilder(uei, SOURCE);
        builder.addParam(PARM_KAR_NAME, karName);
        builder.addParam(PARM_FEATURES, features == null ? "" : String.join(",", features));
        if (reason != null) {
            builder.addParam(PARM_REASON, reason);
        }
        builder.addParam(PARM_USER, user == null ? "unknown" : user);
        builder.addParam(PARM_SOURCE, source == null ? PluginRegistry.SOURCE_UPLOAD : source);
        return builder.getEvent();
    }
}
