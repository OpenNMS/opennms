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

package org.opennms.features.apilayer.alarms;

import java.util.Objects;

import org.opennms.features.apilayer.utils.InterfaceMapper;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.model.Alarm;
import org.opennms.integration.api.v1.model.DatabaseEvent;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.opennms.netmgt.alarmd.api.AlarmPersisterExtension;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.xml.event.Event;
import org.osgi.framework.BundleContext;

public class AlarmPersisterExtensionManager extends InterfaceMapper<org.opennms.integration.api.v1.alarms.AlarmPersisterExtension,AlarmPersisterExtension> {

    private final SessionUtils sessionUtils;

    public AlarmPersisterExtensionManager(BundleContext bundleContext, SessionUtils sessionUtils) {
        super(AlarmPersisterExtension.class, bundleContext);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    @Override
    public AlarmPersisterExtension map(org.opennms.integration.api.v1.alarms.AlarmPersisterExtension ext) {
        return new AlarmPersisterExtension() {
            @Override
            public void afterAlarmCreated(OnmsAlarm alarm, Event event, OnmsEvent dbEvent) {
                final Alarm mappedAlarm = ModelMappers.toAlarm(alarm);
                final InMemoryEvent inMemoryEvent = ModelMappers.toEvent(event);
                final DatabaseEvent databaseEvent = ModelMappers.toEvent(dbEvent);
                final Alarm updatedAlarm = ext.afterAlarmCreated(mappedAlarm, inMemoryEvent, databaseEvent);

                maybeUpdateAlarm(alarm, updatedAlarm);
            }

            @Override
            public void afterAlarmUpdated(OnmsAlarm alarm, Event event, OnmsEvent dbEvent) {
                final Alarm mappedAlarm = ModelMappers.toAlarm(alarm);
                final InMemoryEvent inMemoryEvent = ModelMappers.toEvent(event);
                final DatabaseEvent databaseEvent = ModelMappers.toEvent(dbEvent);
                final Alarm updatedAlarm = ext.afterAlarmUpdated(mappedAlarm, inMemoryEvent, databaseEvent);
                maybeUpdateAlarm(alarm, updatedAlarm);
            }
        };
    }

    private void maybeUpdateAlarm(OnmsAlarm alarm, Alarm updatedAlarm) {
        if (updatedAlarm == null) {
            // The alarm was not updated, nothing to do here
            return;
        }
        sessionUtils.withManualFlush(() -> {
            alarm.setManagedObjectInstance(updatedAlarm.getManagedObjectInstance());
            alarm.setManagedObjectType(updatedAlarm.getManagedObjectType());
            return null;
        });
    }
}
