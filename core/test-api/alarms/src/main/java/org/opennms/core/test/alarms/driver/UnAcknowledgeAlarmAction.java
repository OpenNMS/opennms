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
package org.opennms.core.test.alarms.driver;

import java.util.Date;
import java.util.Objects;

public class UnAcknowledgeAlarmAction implements Action {
    private final String ackUser;
    private final Date ackTime;
    private final String reductionKey;


    public UnAcknowledgeAlarmAction(String ackUser, Date ackTime, String reductionKey) {
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
        visitor.unacknowledgeAlarm(ackUser, ackTime, (a) -> Objects.equals(reductionKey, a.getReductionKey()));
    }
}
