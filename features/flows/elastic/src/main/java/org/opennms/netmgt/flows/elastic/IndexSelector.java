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

package org.opennms.netmgt.flows.elastic;

import static org.opennms.plugins.elasticsearch.rest.index.IndexStrategy.DAILY;
import static org.opennms.plugins.elasticsearch.rest.index.IndexStrategy.HOURLY;
import static org.opennms.plugins.elasticsearch.rest.index.IndexStrategy.MONTHLY;
import static org.opennms.plugins.elasticsearch.rest.index.IndexStrategy.YEARLY;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;

public class IndexSelector {

    private final static Map<IndexStrategy, TemporalUnit> UNIT_MAP;
    static {
        UNIT_MAP = new HashMap<>();
        UNIT_MAP.put(YEARLY, ChronoUnit.YEARS);
        UNIT_MAP.put(MONTHLY, ChronoUnit.MONTHS);
        UNIT_MAP.put(DAILY, ChronoUnit.DAYS);
        UNIT_MAP.put(HOURLY, ChronoUnit.HOURS);

    }

    private String prefix;
    private IndexStrategy strategy;

    public IndexSelector(String prefix, IndexStrategy strategy){
        this.prefix = prefix;
        this.strategy = strategy;
    }

    /**
     * We assume that timeRange is valid:
     * - not Null
     * - start and end date is positive
     * - start date <= end date
     *
     * We also make a big assumption about the time zone:
     * The time system default time zone didn't change between creating the index and querying the index
     */
    public List<String> getIndexNames(TimeRangeFilter timeRange) {
        List<String> all = new ArrayList<>();
        Date endDate = new Date(timeRange.getEnd());
        Date currentDate = new Date(timeRange.getStart());
        TemporalUnit unit = getUnit(strategy);
        while (currentDate.before(endDate)) {
            all.add(strategy.getIndex(prefix, currentDate));
            LocalDateTime current = LocalDateTime.ofInstant(Instant.ofEpochMilli(currentDate.getTime()), ZoneId.systemDefault())
                            .plus(1L, unit);
            currentDate = new Date(current.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        return all;
    }

    private TemporalUnit getUnit(IndexStrategy strategy){
        TemporalUnit unit = UNIT_MAP.get(strategy);
        if(unit == null){
            // should never happen
            throw new UnsupportedOperationException("This is a programming mistake, please check your code!");
        }
        return unit;
    }
}
