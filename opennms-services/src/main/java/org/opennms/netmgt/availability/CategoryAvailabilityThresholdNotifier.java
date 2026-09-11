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
package org.opennms.netmgt.availability;

import java.util.Objects;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.availability.CategoryAvailability;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Emits an event when a category's availability drops below the warning
 * threshold configured for it in categories.xml, and a matching event when
 * it climbs back to or above that threshold.
 *
 * The transition is judged against the previously stored snapshot, so it is
 * detected across restarts and no in-memory state is kept. A category that is
 * already below the threshold when it is first computed raises the event too,
 * so the alarm exists from the start.
 */
public class CategoryAvailabilityThresholdNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(CategoryAvailabilityThresholdNotifier.class);

    public static final String BELOW_WARNING_UEI = "uei.opennms.org/availability/categoryBelowWarningThreshold";
    public static final String ABOVE_WARNING_UEI = "uei.opennms.org/availability/categoryAboveWarningThreshold";
    public static final String EVENT_SOURCE = "OpenNMS.Availability";

    public static final String PARM_LABEL = "label";
    public static final String PARM_AVAILABILITY = "availability";
    public static final String PARM_PREVIOUS_AVAILABILITY = "previousAvailability";
    public static final String PARM_WARNING_THRESHOLD = "warningThreshold";
    public static final String PARM_NORMAL_THRESHOLD = "normalThreshold";
    public static final String PARM_NODE_COUNT = "nodeCount";
    public static final String PARM_SERVICE_COUNT = "serviceCount";
    public static final String PARM_SERVICES_DOWN = "servicesDown";
    public static final String PARM_WINDOW_START = "windowStart";
    public static final String PARM_WINDOW_END = "windowEnd";

    private final EventForwarder m_eventForwarder;

    public CategoryAvailabilityThresholdNotifier(final EventForwarder eventForwarder) {
        m_eventForwarder = Objects.requireNonNull(eventForwarder, "eventForwarder");
    }

    /**
     * Compare the new snapshot with the previous one and send an event if the
     * warning threshold was crossed.
     *
     * @param definition the category, supplying the thresholds
     * @param previous   the snapshot being replaced, or null on the first computation
     * @param current    the snapshot just stored
     */
    public void evaluate(final CategoryDefinition definition, final CategoryAvailability previous, final CategoryAvailability current) {
        final Double warning = definition.getWarningThreshold();
        if (warning == null) {
            return;
        }
        final boolean nowBelow = current.getAvailability() < warning;
        final boolean wasBelow = previous != null && previous.getAvailability() < warning;

        if (nowBelow && !wasBelow) {
            LOG.info("Category '{}' availability {} fell below warning threshold {}", definition.getLabel(), current.getAvailability(), warning);
            m_eventForwarder.sendNow(build(BELOW_WARNING_UEI, definition, previous, current));
        } else if (!nowBelow && wasBelow) {
            LOG.info("Category '{}' availability {} is back at or above warning threshold {}", definition.getLabel(), current.getAvailability(), warning);
            m_eventForwarder.sendNow(build(ABOVE_WARNING_UEI, definition, previous, current));
        }
    }

    private static org.opennms.netmgt.xml.event.Event build(final String uei, final CategoryDefinition definition, final CategoryAvailability previous, final CategoryAvailability current) {
        final EventBuilder builder = new EventBuilder(uei, EVENT_SOURCE);
        builder.addParam(PARM_LABEL, definition.getLabel());
        builder.addParam(PARM_AVAILABILITY, formatPercent(current.getAvailability()));
        if (previous != null) {
            builder.addParam(PARM_PREVIOUS_AVAILABILITY, formatPercent(previous.getAvailability()));
        }
        builder.addParam(PARM_WARNING_THRESHOLD, formatPercent(definition.getWarningThreshold()));
        if (definition.getNormalThreshold() != null) {
            builder.addParam(PARM_NORMAL_THRESHOLD, formatPercent(definition.getNormalThreshold()));
        }
        builder.addParam(PARM_NODE_COUNT, Long.toString(current.getNodeCount()));
        builder.addParam(PARM_SERVICE_COUNT, Long.toString(current.getServiceCount()));
        builder.addParam(PARM_SERVICES_DOWN, Long.toString(current.getServicesDown()));
        builder.addParam(PARM_WINDOW_START, EventConstants.getEventDatetimeFormatter().format(current.getWindowStart()));
        builder.addParam(PARM_WINDOW_END, EventConstants.getEventDatetimeFormatter().format(current.getWindowEnd()));
        return builder.getEvent();
    }

    private static String formatPercent(final double value) {
        return String.format("%.3f", value);
    }
}
