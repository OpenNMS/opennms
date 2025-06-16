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

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.model.OnmsSeverity;

public class SeverityMapper {

    // Maps the Status to an OnmsSeverity
    public static OnmsSeverity toSeverity(Status status) {
        Map<Status, OnmsSeverity> map = new HashMap<>();
        map.put(Status.CRITICAL, OnmsSeverity.CRITICAL);
        map.put(Status.INDETERMINATE, OnmsSeverity.INDETERMINATE);
        map.put(Status.MAJOR, OnmsSeverity.MAJOR);
        map.put(Status.MINOR, OnmsSeverity.MINOR);
        map.put(Status.NORMAL, OnmsSeverity.NORMAL);
        map.put(Status.WARNING, OnmsSeverity.WARNING);

        return map.get(status);
    }

    public static Status toStatus(OnmsSeverity severity) {
        Map<OnmsSeverity, Status> map = new HashMap<>();
        map.put(OnmsSeverity.INDETERMINATE, Status.INDETERMINATE);
        map.put(OnmsSeverity.CLEARED, Status.INDETERMINATE);
        map.put(OnmsSeverity.NORMAL, Status.NORMAL);
        map.put(OnmsSeverity.WARNING, Status.WARNING);
        map.put(OnmsSeverity.MINOR, Status.MINOR);
        map.put(OnmsSeverity.MAJOR, Status.MAJOR);
        map.put(OnmsSeverity.CRITICAL, Status.CRITICAL);
        return map.get(severity);
    }
}
