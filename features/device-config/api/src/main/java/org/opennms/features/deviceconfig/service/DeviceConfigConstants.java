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
package org.opennms.features.deviceconfig.service;

public interface DeviceConfigConstants {
    static final String TRIGGERED_POLL = "dcbTriggeredPoll";
    static final String CONFIG_TYPE = "config-type";
    static final String SCHEDULE = "schedule";
    static final String DEFAULT_CRON_SCHEDULE = "0 0 0 * * ?";
    static final String NEVER = "never";
    static final String RETENTION_PERIOD = "retention-period";
    static final String REST = "REST";
    static final String CRON = "cron";
    static final String PARM_DEVICE_CONFIG_BACKUP_START_TIME = "backupStartTime";
    static final String PARM_DEVICE_CONFIG_BACKUP_DATA_PROTOCOL = "backupDataProtocol";
    static final String PARM_DEVICE_CONFIG_BACKUP_CONTROL_PROTOCOL = "backupControlProtocol";
}
