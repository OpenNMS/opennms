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

package org.opennms.features.kafka.producer.datasync;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.kafka.producer.model.OpennmsModelProtos;
import org.opennms.netmgt.model.OnmsAlarm;

public class AlarmSyncResults {
    private final Map<String, OpennmsModelProtos.Alarm> alarmsInKtableByReductionKey;
    private final List<OnmsAlarm> alarmsInDb;
    private final Map<String, OnmsAlarm> alarmsInDbByReductionKey;
    private final Set<String> reductionKeysAdded;
    private final Set<String> reductionKeysDeleted;
    private final Set<String> reductionKeysUpdated;

    public AlarmSyncResults(Map<String, OpennmsModelProtos.Alarm> alarmsInKtableByReductionKey,
                            List<OnmsAlarm> alarmsInDb,
                            Map<String, OnmsAlarm> alarmsInDbByReductionKey,
                            Set<String> reductionKeysAdded,
                            Set<String> reductionKeysDeleted,
                            Set<String> reductionKeysUpdated) {
        this.alarmsInKtableByReductionKey = Objects.requireNonNull(alarmsInKtableByReductionKey);
        this.alarmsInDb = Objects.requireNonNull(alarmsInDb);
        this.alarmsInDbByReductionKey = Objects.requireNonNull(alarmsInDbByReductionKey);
        this.reductionKeysAdded = Objects.requireNonNull(reductionKeysAdded);
        this.reductionKeysDeleted = Objects.requireNonNull(reductionKeysDeleted);
        this.reductionKeysUpdated = Objects.requireNonNull(reductionKeysUpdated);
    }

    public Map<String, OpennmsModelProtos.Alarm> getAlarmsInKtableByReductionKey() {
        return alarmsInKtableByReductionKey;
    }

    public List<OnmsAlarm> getAlarmsInDb() {
        return alarmsInDb;
    }

    public Map<String, OnmsAlarm> getAlarmsInDbByReductionKey() {
        return alarmsInDbByReductionKey;
    }

    public Set<String> getReductionKeysAdded() {
        return reductionKeysAdded;
    }

    public Set<String> getReductionKeysDeleted() {
        return reductionKeysDeleted;
    }

    public Set<String> getReductionKeysUpdated() {
        return reductionKeysUpdated;
    }

    public int getNumUpdates() {
        return getReductionKeysAdded().size()
                + getReductionKeysDeleted().size()
                + getReductionKeysUpdated().size();
    }
}
