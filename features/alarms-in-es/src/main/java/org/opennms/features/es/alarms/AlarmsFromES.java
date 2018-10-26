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

package org.opennms.features.es.alarms;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.es.alarms.dto.AlarmDocumentDTO;
import org.opennms.netmgt.model.OnmsAlarm;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.TermsAggregation;
import io.searchbox.core.search.aggregation.TopHitsAggregation;

public class AlarmsFromES {

    private final JestClient client;

    private final QueryProvider queryProvider = new QueryProvider();

    public AlarmsFromES(JestClient client) {
        this.client = Objects.requireNonNull(client);
    }

    public AlarmDocumentDTO getAlarmAt(int id, long time) {
        return findAlarms(queryProvider.getAlarmAt(id, time), false).stream().findFirst().orElse(null);
    }

    public List<AlarmDocumentDTO> getAlarmsAt(long time) {
        return findAlarms(queryProvider.getAlarmsAt(time), true);
    }

    public List<AlarmDocumentDTO> getAllAlarms() {
        return findAlarms(queryProvider.getAllAlarms(), true);
    }

    public List<AlarmDocumentDTO> getCurrentAlarms() {
        return findAlarms(queryProvider.getCurrentAlarms(), true);
    }

    public AlarmDocumentDTO getProblemAlarmAt(long t) {
        return getFirstAlarmWithType(t, OnmsAlarm.PROBLEM_TYPE, "problem");
    }

    public AlarmDocumentDTO getResolutionAlarmAt(long t) {
        return getFirstAlarmWithType(t, OnmsAlarm.RESOLUTION_TYPE, "resolution");
    }

    public List<AlarmDocumentDTO> getSituationsAt(long time) {
        return getAlarmsAt(time).stream()
                .filter(AlarmDocumentDTO::isSituation)
                .collect(Collectors.toList());
    }

    public AlarmDocumentDTO getSituationAt(long time) {
        return getSituationsAt(time).stream()
                .findFirst()
                .orElse(null);
    }

    private List<AlarmDocumentDTO> findAlarms(String query, boolean inAggregation) {
        final Search search = new Search.Builder(query)
                .addIndex("opennms-alarms-*")
                .addType(AlarmDocumentDTO.TYPE)
                .build();
        final SearchResult result;
        try {
            result = client.execute(search);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!result.isSucceeded()) {
            throw new RuntimeException(result.getErrorMessage());
        }

        if (!inAggregation) {
            final List<SearchResult.Hit<AlarmDocumentDTO, Void>> hits = result.getHits(AlarmDocumentDTO.class);
            return hits.stream().map(h -> h.source).collect(Collectors.toList());
        } else {
            final List<AlarmDocumentDTO> alarms = new LinkedList<>();
            final TermsAggregation alarmsById = result.getAggregations().getTermsAggregation("alarms_by_id");
            if (alarmsById != null) {
                for (TermsAggregation.Entry entry : alarmsById.getBuckets()) {
                    //final String alarmId = entry.getKey();
                    final TopHitsAggregation topHitsAggregation = entry.getTopHitsAggregation("latest_alarm");
                    final List<SearchResult.Hit<AlarmDocumentDTO, Void>> hits = topHitsAggregation.getHits(AlarmDocumentDTO.class);
                    hits.stream().map(h -> h.source).forEach(alarms::add);
                }
            }
            return alarms;
        }
    }

    private AlarmDocumentDTO getFirstAlarmWithType(long time, int type, String typeDescr) {
        return getAlarmsAt(time).stream()
                .filter(doc -> doc.getType() == type)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No " + typeDescr + " alarms at time: " + time));
    }


}
