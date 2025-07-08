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
package org.opennms.netmgt.model;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

public class SituationPayload {

    private final List<Integer> alarmIdList;
    private final String diagnosticText;
    private final String description;
    private final String feedback;

    @JsonCreator
    public SituationPayload(
            @JsonProperty("alarmIdList") List<Integer> alarmIdList,
            @JsonProperty("diagnosticText") String diagnosticText,
            @JsonProperty("description") String description,
            @JsonProperty("feedback") String feedback) {
        this.alarmIdList = alarmIdList;
        this.diagnosticText = diagnosticText;
        this.description = description;
        this.feedback = feedback;
    }

    public List<Integer> getAlarmIdList() {
        return alarmIdList;
    }

    public String getDiagnosticText() {
        return diagnosticText;
    }

    public String getDescription() {
        return description;
    }

    public String getFeedback() {
        return feedback;
    }
}
