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
package org.opennms.features.topology.plugins.browsers;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;

public class AlarmCellStyleRenderer {

    public String getStyle(Integer severityObject, boolean acknowledged) {
        int severity = severityObject == null ? OnmsSeverity.INDETERMINATE.getId() : severityObject.intValue();
        final StringBuilder retVal = new StringBuilder();
        
        if (OnmsSeverity.CLEARED.getId() == severity) {
        } else if (OnmsSeverity.CRITICAL.getId() == severity) {
            retVal.append("alarm-critical");
        } else if (OnmsSeverity.INDETERMINATE.getId() == severity) {
            retVal.append("alarm-indeterminate");
        } else if (OnmsSeverity.MAJOR.getId() == severity) {
            retVal.append("alarm-major");
        } else if (OnmsSeverity.MINOR.getId() == severity) {
            retVal.append("alarm-minor");
        } else if (OnmsSeverity.NORMAL.getId() == severity) {
            retVal.append("alarm-normal");
        } else if (OnmsSeverity.WARNING.getId() == severity) {
            retVal.append("alarm-warning");
        }

        if (!acknowledged) {
            retVal.append("-noack");
        }
        return retVal.toString();
    }

    public String getStyle(OnmsAlarm alarm) {
        if (alarm == null) return getStyle(null, false);
        return getStyle(alarm.getSeverityId(), alarm.isAcknowledged());
    }
}
