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

package org.opennms.web.rest.v2;

import java.util.List;

public class AlarmActionRequest {
    public enum Action { ACK, UNACK, ESCALATE, CLEAR, ACCEPT /* etc */ }

    private Integer situationId;
    private Boolean value = Boolean.TRUE;
    private  List<Integer> alarmIdList;

    public Integer getSituationId() {
        return situationId;
    }

    public List<Integer> getAlarmIdList() {
        return alarmIdList;
    }

    public void setAlarmIdList(List<Integer> alarmIdList) {
        this.alarmIdList = alarmIdList;
    }

    public void setSituationId(Integer situationId) {
        this.situationId = situationId;
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        this.value = value;
    }
}