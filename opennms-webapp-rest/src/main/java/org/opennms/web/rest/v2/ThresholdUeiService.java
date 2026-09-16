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

import java.util.List;

import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.config.threshd.Basethresholddef;
import org.opennms.netmgt.config.threshd.ThresholdType;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creates event definitions for the custom UEIs a threshold names.
 *
 * <p>A threshold may declare its own {@code triggeredUEI}/{@code rearmedUEI} instead of using the built-in
 * threshold events. If eventconf has no definition for such a UEI, the event still gets sent but carries no
 * description, no severity and no alarm data, so it raises no alarm and fires no notification — and the user
 * gets no indication that anything is wrong. To avoid that, a definition is generated here by cloning the
 * built-in threshold event that matches the threshold's type.</p>
 *
 * <p>Ported from the JSP threshold editor's controller, which was the only place this behaviour lived.</p>
 */
@Service
public class ThresholdUeiService {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdUeiService.class);

    private static final String AUTHOR = "Web UI";

    @Autowired
    private EventConfDao eventConfDao;

    @Autowired
    private EventConfPersistenceService eventConfPersistenceService;

    /**
     * Ensures eventconf knows the triggered and rearmed UEIs of the given threshold definition, creating
     * definitions for any that are missing.
     *
     * <p>Call this with the mapped entity rather than the request DTO: the mapper has already dropped the
     * rearmed UEI for the threshold types that never re-arm, so this method does not have to know about
     * that rule a second time.</p>
     */
    public void ensureUeisInEventConf(final Basethresholddef def) {
        if (def == null || def.getType() == null) {
            return;
        }

        String clearKey = null;

        final String triggeredUei = def.getTriggeredUEI().orElse(null);
        if (triggeredUei != null) {
            final Event source = getSourceEvent(def.getType(), true);
            if (source != null && source.getAlarmData() != null && source.getAlarmData().getReductionKey() != null) {
                clearKey = source.getAlarmData().getReductionKey().replace("%uei%", triggeredUei);
            }
            ensureUeiInEventConf(source, triggeredUei, def.getType(), null, true);
        }

        final String rearmedUei = def.getRearmedUEI().orElse(null);
        if (rearmedUei != null) {
            final Event source = getSourceEvent(def.getType(), false);
            ensureUeiInEventConf(source, rearmedUei, def.getType(), clearKey, false);
        }
    }

    /**
     * @return the built-in threshold event a generated definition should be cloned from, or null when there
     *         is none (the change thresholds have no rearm event, and eventconf may simply not hold it)
     */
    Event getSourceEvent(final ThresholdType thresholdType, final boolean isTrigger) {
        String sourceUei = null;

        switch (thresholdType) {
            case HIGH:
                sourceUei = isTrigger ? EventConstants.HIGH_THRESHOLD_EVENT_UEI : EventConstants.HIGH_THRESHOLD_REARM_EVENT_UEI;
                break;
            case LOW:
                sourceUei = isTrigger ? EventConstants.LOW_THRESHOLD_EVENT_UEI : EventConstants.LOW_THRESHOLD_REARM_EVENT_UEI;
                break;
            case RELATIVE_CHANGE:
                sourceUei = isTrigger ? EventConstants.RELATIVE_CHANGE_THRESHOLD_EVENT_UEI : null;
                break;
            case ABSOLUTE_CHANGE:
                sourceUei = isTrigger ? EventConstants.ABSOLUTE_CHANGE_THRESHOLD_EVENT_UEI : null;
                break;
            case REARMING_ABSOLUTE_CHANGE:
                sourceUei = isTrigger ? EventConstants.REARMING_ABSOLUTE_CHANGE_EXCEEDED_EVENT_UEI
                        : EventConstants.REARMING_ABSOLUTE_CHANGE_REARM_EVENT_UEI;
                break;
        }

        if (sourceUei == null) {
            return null;
        }

        final List<Event> eventsForUei = eventConfDao.getEvents(sourceUei);
        if (eventsForUei != null && !eventsForUei.isEmpty()) {
            return eventsForUei.get(0);
        }
        return null;
    }

    private void ensureUeiInEventConf(final Event source, final String targetUei, final ThresholdType thresholdType,
                                      final String clearKey, final boolean isTrigger) {
        final List<Event> eventsForUei = eventConfDao.getEvents(targetUei);
        if (eventsForUei != null && !eventsForUei.isEmpty()) {
            return;
        }

        final String typeDesc = isTrigger ? "exceeded" : "rearmed";
        final Event event = new Event();
        event.setUei(targetUei);
        event.setEventLabel("User-defined " + thresholdType + " threshold event " + typeDesc + ": " + targetUei);

        final Logmsg logmsg = new Logmsg();
        event.setLogmsg(logmsg);

        if (source == null) {
            final String text = "Threshold " + typeDesc
                    + " for %service% datasource %parm[ds]% on interface %interface%, parms: %parm[all]%";
            event.setDescr(text);
            logmsg.setDest(LogDestType.LOGNDISPLAY);
            logmsg.setContent(text);
            event.setSeverity("Warning");
        } else {
            event.setDescr(source.getDescr());
            event.setSeverity(source.getSeverity());
            event.setOperinstruct(source.getOperinstruct());
            logmsg.setDest(source.getLogmsg().getDest());
            logmsg.setContent(source.getLogmsg().getContent());

            if (source.getAlarmData() != null) {
                final AlarmData sourceAlarmData = source.getAlarmData();
                final AlarmData alarmData = new AlarmData();
                alarmData.setAlarmType(sourceAlarmData.getAlarmType());
                alarmData.setAutoClean(sourceAlarmData.getAutoClean());
                alarmData.setReductionKey(sourceAlarmData.getReductionKey());
                if (!isTrigger && clearKey != null) {
                    alarmData.setClearKey(clearKey);
                }
                event.setAlarmData(alarmData);
            }
        }

        LOG.info("Creating event definition for user-defined threshold UEI {}.", targetUei);
        eventConfPersistenceService.saveProgrammaticEvent(event, AUTHOR);
        eventConfPersistenceService.reloadEventsIntoMemory();
    }
}
