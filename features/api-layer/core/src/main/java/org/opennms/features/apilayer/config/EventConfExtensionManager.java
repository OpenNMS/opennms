/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.apilayer.config;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.config.events.EventConfExtension;
import org.opennms.integration.api.v1.config.events.EventDefinition;
import org.opennms.integration.api.v1.config.events.LogMsgDestType;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.netmgt.xml.eventconf.LogDestType;
import org.opennms.netmgt.xml.eventconf.Logmsg;
import org.opennms.netmgt.xml.eventconf.ManagedObject;
import org.opennms.netmgt.xml.eventconf.Mask;
import org.opennms.netmgt.xml.eventconf.Maskelement;
import org.opennms.netmgt.xml.eventconf.Parameter;
import org.opennms.netmgt.xml.eventconf.UpdateField;
import org.opennms.netmgt.xml.eventconf.Varbind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventConfExtensionManager extends ConfigExtensionManager<EventConfExtension, Events> {
    private static final Logger LOG = LoggerFactory.getLogger(EventConfExtensionManager.class);

    private final EventConfDao eventConfDao;

    public EventConfExtensionManager(EventConfDao eventConfDao) {
        super(Events.class, new Events());
        this.eventConfDao = Objects.requireNonNull(eventConfDao);
        LOG.debug("EventConfExtensionManager initialized.");
    }

    @Override
    protected Events getConfigForExtensions(Set<EventConfExtension> extensions) {
        final List<Event> orderedEvents = extensions.stream()
                .flatMap(ext -> ext.getEventDefinitions().stream())
                .sorted(Comparator.comparing(EventDefinition::getPriority))
                .map(EventConfExtensionManager::toEvent)
                .collect(Collectors.toList());
        // Re-build the events
        final Events events = new Events();
        events.getEvents().addAll(orderedEvents);
        return events;
    }

    @Override
    protected void triggerReload() {
        LOG.debug("Event configuration changed. Triggering a reload.");
        eventConfDao.reload();
    }

    private static Event toEvent(EventDefinition def) {
        final Event event = new Event();
        event.setMask(toMask(def.getMask()));
        event.setUei(def.getUei());
        event.setPriority(def.getPriority());
        event.setEventLabel(def.getLabel());
        event.setDescr(def.getDescription());
        event.setSeverity(def.getSeverity().getLabel());
        event.setLogmsg(toLogMsg(def.getLogMessage()));
        event.setOperinstruct(def.getOperatorInstructions());
        event.setAlarmData(toAlarmData(def.getAlarmData()));
        final List<Parameter> parms = def.getParameters().stream()
                .map(EventConfExtensionManager::toParameter)
                .collect(Collectors.toList());
        event.setParameters(parms);
        return event;
    }

    private static Logmsg toLogMsg(org.opennms.integration.api.v1.config.events.LogMessage l) {
        final Logmsg logmsg = new Logmsg();
        logmsg.setContent(l.getContent());
        logmsg.setDest(toLogDestType(l.getDestination()));
        return logmsg;
    }

    private static LogDestType toLogDestType(LogMsgDestType type) {
        if (type != null) {
            switch(type) {
                case LOGNDISPLAY:
                    return LogDestType.LOGNDISPLAY;
                case DISPLAYONLY:
                    return LogDestType.DISPLAYONLY;
                case LOGONLY:
                    return LogDestType.LOGONLY;
                case SUPPRESS:
                    return LogDestType.SUPPRESS;
                case DONOTPERSIST:
                    return LogDestType.DONOTPERSIST;
                case DISCARDTRAPS:
                    return LogDestType.DISCARDTRAPS;
            }
        }
        return LogDestType.LOGNDISPLAY;
    }

    private static Mask toMask(org.opennms.integration.api.v1.config.events.Mask m) {
        if (m == null) {
            return null;
        }
        final Mask mask = new Mask();
        mask.setMaskelements(m.getMaskElements().stream()
                .map(EventConfExtensionManager::toMaskElement)
                .collect(Collectors.toList()));
        mask.setVarbinds(m.getVarbinds().stream()
                .map(EventConfExtensionManager::toVarbind)
                .collect(Collectors.toList()));
        return mask;
    }

    private static Maskelement toMaskElement(org.opennms.integration.api.v1.config.events.MaskElement el) {
        final Maskelement maskEl = new Maskelement();
        maskEl.setMename(el.getName());
        maskEl.setMevalues(el.getValues());
        return maskEl;
    }

    private static Varbind toVarbind(org.opennms.integration.api.v1.config.events.Varbind vb) {
        final Varbind varbind = new Varbind();
        varbind.setVbnumber(vb.getNumber());
        varbind.setTextualConvention(vb.getTextualConvention());
        varbind.setVbvalues(vb.getValues());
        return varbind;
    }

    private static AlarmData toAlarmData(org.opennms.integration.api.v1.config.events.AlarmData alarm) {
        if (alarm == null) {
            return null;
        }
        final AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey(alarm.getReductionKey());
        alarmData.setClearKey(alarm.getClearKey());
        if (alarm.getType() != null) {
            alarmData.setAlarmType(alarm.getType().getId());
        }
        alarmData.setAutoClean(alarm.isAutoClean());
        final List<UpdateField> updateFields = alarm.getUpdateFields().stream()
                .map(EventConfExtensionManager::toUpdateField)
                .collect(Collectors.toList());
        alarmData.setUpdateFields(updateFields);
        alarmData.setManagedObject(toManagedObject(alarm.getManagedObject()));
        return alarmData;
    }

    private static UpdateField toUpdateField(org.opennms.integration.api.v1.config.events.UpdateField u) {
        final UpdateField updateField = new UpdateField();
        updateField.setFieldName(u.getName());
        updateField.setUpdateOnReduction(u.isUpdatedOnReduction());
        return updateField;
    }

    private static Parameter toParameter(org.opennms.integration.api.v1.config.events.Parameter p) {
        final Parameter parm = new Parameter();
        parm.setName(p.getName());
        parm.setValue(p.getValue());
        parm.setExpand(p.shouldExpand());
        return parm;
    }

    private static ManagedObject toManagedObject(org.opennms.integration.api.v1.config.events.ManagedObject mo) {
        if (mo == null) {
            return null;
        }
        final ManagedObject managedObject = new ManagedObject();
        managedObject.setType(mo.getType());
        return managedObject;
    }
}
