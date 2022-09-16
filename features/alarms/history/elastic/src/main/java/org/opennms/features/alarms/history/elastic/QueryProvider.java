/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.alarms.history.elastic;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Here we store the search queries in the class-path in the form
 * of Freemarker templates and use the templating to perform the parameter
 * substitution.
 *
 * This makes it cleaner than storing the search queries as multiline strings
 * in Java code, and is much less verbose than storing these as POJOs.
 */
public class QueryProvider {

    /**
     * Maximum number of states to return when querying the states for a single alarm.
     */
    public static final long MAX_STATES_FOR_ALARM = 100;

    /**
     * Maximum number of buckets that can be processed in one request.
     * Subsequent requests should be made to page through the results
     */
    public static final long MAX_BUCKETS = 1000;
    public static final String FROM_MILLIS = "fromMillis";
    public static final String TO_MILLIS = "toMillis";
    public static final String MAX_BUCKETS1 = "maxBuckets";
    public static final String ID_ONLY = "idOnly";
    public static final String AFTER_ALARM_WITH_ID = "afterAlarmWithId";
    public static final String GET_ALARMS_AT_FTL = "get_alarms_at.ftl";

    private final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    public QueryProvider() {
        // Setup Freemarker
        cfg.setClassForTemplateLoading(getClass(), "");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public String getAlarmStatesByDbId(long id) {
        return render("get_alarm.ftl", ImmutableMap.builder()
                .put("alarmId", id)
                .put("maxResults", MAX_STATES_FOR_ALARM)
                .build());
    }

    public String getAlarmStatesByReductionKey(String reductionKey) {
        return render("get_alarm.ftl", ImmutableMap.builder()
                .put("reductionKey", reductionKey)
                .put("maxResults", MAX_STATES_FOR_ALARM)
                .build());
    }

    public String getAlarmByDbIdAt(long id, TimeRange timeRange) {
        return render("get_alarm_at.ftl", ImmutableMap.builder()
                .put("alarmId", id)
                .put(FROM_MILLIS, timeRange.getStart())
                .put(TO_MILLIS, timeRange.getEnd())
                .build());
    }

    public String getAlarmByReductionKeyAt(String reductionKey, TimeRange timeRange) {
        return render("get_alarm_at.ftl", ImmutableMap.builder()
                .put("reductionKey", reductionKey)
                .put(FROM_MILLIS, timeRange.getStart())
                .put(TO_MILLIS, timeRange.getEnd())
                .build());
    }

    public String getActiveAlarmsAt(TimeRange timeRange, Integer afterAlarmWithId) {
        final ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put(FROM_MILLIS, timeRange.getStart())
                .put(TO_MILLIS, timeRange.getEnd())
                .put(MAX_BUCKETS1, MAX_BUCKETS)
                .put(ID_ONLY, false);
        if (afterAlarmWithId != null) {
            builder.put(AFTER_ALARM_WITH_ID, afterAlarmWithId);
        }
        return render(GET_ALARMS_AT_FTL, builder.build());
    }

    public String getActiveAlarmIdsAt(TimeRange timeRange, Integer afterAlarmWithId) {
        final ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put(FROM_MILLIS, timeRange.getStart())
                .put(TO_MILLIS, timeRange.getEnd())
                .put(MAX_BUCKETS1, MAX_BUCKETS)
                .put(ID_ONLY, true);
        if (afterAlarmWithId != null) {
            builder.put(AFTER_ALARM_WITH_ID, afterAlarmWithId);
        }
        return render(GET_ALARMS_AT_FTL, builder.build());
    }

    public String getActiveAlarmIdsAtTimeAndExclude(TimeRange timeRange, Set<Integer> alarmIdsToKeep, Integer afterAlarmWithId) {
        final ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put(FROM_MILLIS, timeRange.getStart())
                .put(TO_MILLIS, timeRange.getEnd())
                .put(MAX_BUCKETS1, MAX_BUCKETS)
                .put("alarmIdsToExclude", alarmIdsToKeep)
                .put(ID_ONLY, true);
        if (afterAlarmWithId != null) {
            builder.put(AFTER_ALARM_WITH_ID, afterAlarmWithId);
        }
        return render(GET_ALARMS_AT_FTL, builder.build());
    }

    public String getAllAlarms(TimeRange timeRange, Integer afterAlarmWithId) {
        ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put(FROM_MILLIS, timeRange.getStart())
                .put(TO_MILLIS, timeRange.getEnd())
                .put(MAX_BUCKETS1, MAX_BUCKETS);
        if (afterAlarmWithId != null) {
            builder.put(AFTER_ALARM_WITH_ID, afterAlarmWithId);
        }
        return render("get_all_alarms.ftl", builder.build());
    }

    private String render(String templateName, Map<Object, Object> context) {
        try {
            final StringWriter writer = new StringWriter();
            final Template template = cfg.getTemplate(templateName);
            template.process(context, writer);
            return writer.toString();
        } catch (IOException|TemplateException e) {
            throw new ElasticException(e);
        }
    }

}
