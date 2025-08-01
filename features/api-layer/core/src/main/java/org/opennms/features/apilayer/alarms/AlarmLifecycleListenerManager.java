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

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
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
