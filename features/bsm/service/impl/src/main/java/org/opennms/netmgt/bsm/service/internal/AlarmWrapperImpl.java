/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.internal;

import java.util.Objects;

import org.opennms.netmgt.bsm.service.model.AlarmWrapper;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.model.OnmsAlarm;

public class AlarmWrapperImpl implements AlarmWrapper {

    private final String m_reductionKey;
    private final Status m_status;
    private final Integer m_id;

    public AlarmWrapperImpl(OnmsAlarm alarm) {
        Objects.requireNonNull(alarm);
        m_reductionKey = alarm.getReductionKey();
        m_status = SeverityMapper.toStatus(alarm.getSeverity());
        m_id = alarm.getId();
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
    public Integer getId() {
        return m_id;
    }
}
