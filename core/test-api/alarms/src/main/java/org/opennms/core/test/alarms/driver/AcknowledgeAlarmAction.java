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

package org.opennms.core.test.alarms.driver;

import java.util.Date;
import java.util.Objects;

public class AcknowledgeAlarmAction implements Action {
    private final String ackUser;
    private final Date ackTime;
    private final String reductionKey;

    public AcknowledgeAlarmAction(String ackUser, Date ackTime, String reductionKey) {
        this.ackUser = Objects.requireNonNull(ackUser);
        this.ackTime = Objects.requireNonNull(ackTime);
        this.reductionKey = Objects.requireNonNull(reductionKey);
    }

    @Override
    public Date getTime() {
        return ackTime;
    }

    @Override
    public void visit(ActionVisitor visitor) {
        visitor.acknowledgeAlarm(ackUser, ackTime, (a) -> Objects.equals(reductionKey, a.getReductionKey()));
    }
}
