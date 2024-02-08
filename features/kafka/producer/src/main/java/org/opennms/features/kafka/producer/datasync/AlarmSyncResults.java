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
