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
        return com.google.common.base.Objects.toStringHelper(this)
                .add("situationId", situationId)
                .add("situationSeverity", situationSeverity)
                .add("situationLocations", situationLocations)
                .add("affectedNodes", affectedNodes)
                .add("relatedAlarms", relatedAlarms)
                .toString();
    }
}
