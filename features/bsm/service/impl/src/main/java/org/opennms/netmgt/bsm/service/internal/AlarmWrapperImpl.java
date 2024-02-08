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
package org.opennms.netmgt.bsm.service.internal;

import java.util.Objects;

import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.model.OnmsAlarm;

public class AlarmWrapperImpl implements AlarmWrapper {

    private final String m_reductionKey;
    private final Status m_status;

    public AlarmWrapperImpl(OnmsAlarm alarm) {
        Objects.requireNonNull(alarm);
        m_reductionKey = alarm.getReductionKey();
        m_status = SeverityMapper.toStatus(alarm.getSeverity());
    }

    @Override
    public String getReductionKey() {
        return m_reductionKey;
    }

    @Override
    public Status getStatus() {
        return m_status;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof AlarmWrapperImpl)) {
            return false;
        }
        AlarmWrapperImpl castOther = (AlarmWrapperImpl) other;
        return Objects.equals(m_reductionKey, castOther.m_reductionKey) && Objects.equals(m_status, castOther.m_status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_reductionKey, m_status);
    }

    @Override
    public String toString() {
        return String.format("AlarmWrapperImpl[reductionKey=%s, status=%s]", m_reductionKey, m_status);
    }

}
