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
package org.opennms.features.apilayer.alarms;

import java.util.Objects;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
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
