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
package org.opennms.netmgt.model.alarm;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.utils.FuzzyDateFormatter;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.Lists;

public class SituationSummary {
    private int situationId;
    private OnmsSeverity situationSeverity;
    private String situationLocations;
    private long affectedNodes;
    private long relatedAlarms;
    private Date minLastEventDate;

    public SituationSummary(final Integer situationId, final OnmsSeverity situationSeverity, final String situationLocations, final Long affectedNodes, final Long relatedAlarms, final Date minLastEventDate) {
        this.situationId = situationId;
        this.situationSeverity = situationSeverity;
        this.situationLocations = situationLocations;
        this.affectedNodes = affectedNodes;
        this.relatedAlarms = relatedAlarms;
        this.minLastEventDate = minLastEventDate;
    }

    public int getSituationId() {
        return situationId;
    }

    public Date getMinLastEventDate() {
        return minLastEventDate;
    }

    public OnmsSeverity getSituationSeverity() {
        return situationSeverity;
    }

    public String getSituationLocations() {
        return situationLocations;
    }

    public List<String> getSituationLocationList() {
        return situationLocations == null ? Lists.newArrayList() : Stream.of(situationLocations.split(", ")).collect(Collectors.toList());
    }

    public long getAffectedNodes() {
        return affectedNodes;
    }

    public long getRelatedAlarms() {
        return relatedAlarms;
    }

    public String getFuzzyTimeDown() {
        return minLastEventDate == null ? "N/A" : FuzzyDateFormatter.calculateDifference(this.minLastEventDate, new Date());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SituationSummary that = (SituationSummary) o;
        return situationId == that.situationId &&
                affectedNodes == that.affectedNodes &&
                relatedAlarms == that.relatedAlarms &&
                situationSeverity == that.situationSeverity &&
                Objects.equals(situationLocations, that.situationLocations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(situationId, situationSeverity, situationLocations, affectedNodes, relatedAlarms);
    }

    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("situationId", situationId)
                .add("situationSeverity", situationSeverity)
                .add("situationLocations", situationLocations)
                .add("affectedNodes", affectedNodes)
                .add("relatedAlarms", relatedAlarms)
                .toString();
    }
}
