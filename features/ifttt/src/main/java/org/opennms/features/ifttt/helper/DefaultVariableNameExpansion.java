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
package org.opennms.features.ifttt.helper;

import org.opennms.netmgt.model.OnmsSeverity;

/**
 * Default implementation for variable replacements. This class replaces placeholders with the old and new severity and
 * the old and new alarm count.
 */
public class DefaultVariableNameExpansion implements VariableNameExpansion {
    private OnmsSeverity oldSeverity, newSeverity;
    private int oldAlarmCount, newAlarmCount;

    public DefaultVariableNameExpansion(final OnmsSeverity oldSeverity, final OnmsSeverity newSeverity, final int oldAlarmCount, final int newAlarmCount) {
        this.oldSeverity = oldSeverity;
        this.newSeverity = newSeverity;
        this.oldAlarmCount = oldAlarmCount;
        this.newAlarmCount = newAlarmCount;
    }

    @Override
    public String replace(final String string) {
        return string.replace("%os%", oldSeverity.toString())
                .replace("%ns%", newSeverity.toString())
                .replace("%oc%", String.valueOf(oldAlarmCount))
                .replace("%nc%", String.valueOf(newAlarmCount))
                .replace("%oldSeverity%", oldSeverity.toString())
                .replace("%newSeverity%", newSeverity.toString())
                .replace("%oldCount%", String.valueOf(oldAlarmCount))
                .replace("%newCount%", String.valueOf(newAlarmCount));
    }
}
