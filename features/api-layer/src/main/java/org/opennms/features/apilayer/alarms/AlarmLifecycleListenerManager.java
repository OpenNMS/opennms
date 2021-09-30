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

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.apilayer.utils.InterfaceMapper;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.netmgt.alarmd.api.AlarmLifecycleListener;
import org.opennms.netmgt.model.OnmsAlarm;
import org.osgi.framework.BundleContext;

public class AlarmLifecycleListenerManager extends InterfaceMapper<org.opennms.integration.api.v1.alarms.AlarmLifecycleListener,AlarmLifecycleListener> {

    public AlarmLifecycleListenerManager(BundleContext bundleContext) {
        super(AlarmLifecycleListener.class, bundleContext);
    }

    @Override
    public AlarmLifecycleListener map(org.opennms.integration.api.v1.alarms.AlarmLifecycleListener ext) {
        return new AlarmLifecycleListener() {
            @Override
            public void handleAlarmSnapshot(List<OnmsAlarm> alarms) {
                ext.handleAlarmSnapshot(alarms.stream().map(ModelMappers::toAlarm)
                        .collect(Collectors.toList()));
            }

            @Override
            public void preHandleAlarmSnapshot() {
                // pass
            }

            @Override
            public void postHandleAlarmSnapshot() {
                // pass
            }

            @Override
            public void handleNewOrUpdatedAlarm(OnmsAlarm alarm) {
                ext.handleNewOrUpdatedAlarm(ModelMappers.toAlarm(alarm));
            }

            @Override
            public void handleDeletedAlarm(int alarmId, String reductionKey) {
                ext.handleDeletedAlarm(alarmId, reductionKey);
            }
        };
    }
}
