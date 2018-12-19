/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

    private static final long MAX_STATES_FOR_ALARM = 100;

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

    public String getAlarmByDbIdAt(long id, long time) {
        return render("get_alarm_at.ftl", ImmutableMap.builder()
                .put("alarmId", id)
                .put("time", time)
                .build());
    }

    public String getAlarmByReductionKeyAt(String reductionKey, long time) {
        return render("get_alarm_at.ftl", ImmutableMap.builder()
                .put("reductionKey", reductionKey)
                .put("time", time)
                .build());
    }

    public String getActiveAlarmsAt(long time, Integer afterAlarmWithId) {
        ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put("time", time)
                .put("idOnly", false);
        if (afterAlarmWithId != null) {
            builder.put("afterAlarmWithId", afterAlarmWithId);
        }
        return render("get_alarms_at.ftl", builder.build());
    }

    public String getActiveAlarmsAtTimeAndExclude(long time, Set<Integer> alarmIdsToKeep, Integer afterAlarmWithId) {

        ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put("time", time)
                .put("alarmIdsToExclude", alarmIdsToKeep)
                .put("idOnly", true);
        if (afterAlarmWithId != null) {
            builder.put("afterAlarmWithId", afterAlarmWithId);
        }
        return render("get_alarms_at.ftl", builder.build());
    }

    public String getAllAlarms(Integer afterAlarmWithId) {
        ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder();
        if (afterAlarmWithId != null) {
            builder.put("afterAlarmWithId", afterAlarmWithId);
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
            throw new RuntimeException(e);
        }
    }

}
