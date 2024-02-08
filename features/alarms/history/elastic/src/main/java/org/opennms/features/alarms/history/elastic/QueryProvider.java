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
                .put("fromMillis", timeRange.getStart())
                .put("toMillis", timeRange.getEnd())
                .build());
    }

    public String getAlarmByReductionKeyAt(String reductionKey, TimeRange timeRange) {
        return render("get_alarm_at.ftl", ImmutableMap.builder()
                .put("reductionKey", reductionKey)
                .put("fromMillis", timeRange.getStart())
                .put("toMillis", timeRange.getEnd())
                .build());
    }

    public String getActiveAlarmsAt(TimeRange timeRange, Integer afterAlarmWithId) {
        final ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put("fromMillis", timeRange.getStart())
                .put("toMillis", timeRange.getEnd())
                .put("maxBuckets", MAX_BUCKETS)
                .put("idOnly", false);
        if (afterAlarmWithId != null) {
            builder.put("afterAlarmWithId", afterAlarmWithId);
        }
        return render("get_alarms_at.ftl", builder.build());
    }

    public String getActiveAlarmIdsAt(TimeRange timeRange, Integer afterAlarmWithId) {
        final ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put("fromMillis", timeRange.getStart())
                .put("toMillis", timeRange.getEnd())
                .put("maxBuckets", MAX_BUCKETS)
                .put("idOnly", true);
        if (afterAlarmWithId != null) {
            builder.put("afterAlarmWithId", afterAlarmWithId);
        }
        return render("get_alarms_at.ftl", builder.build());
    }

    public String getActiveAlarmIdsAtTimeAndExclude(TimeRange timeRange, Set<Integer> alarmIdsToKeep, Integer afterAlarmWithId) {
        final ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put("fromMillis", timeRange.getStart())
                .put("toMillis", timeRange.getEnd())
                .put("maxBuckets", MAX_BUCKETS)
                .put("alarmIdsToExclude", alarmIdsToKeep)
                .put("idOnly", true);
        if (afterAlarmWithId != null) {
            builder.put("afterAlarmWithId", afterAlarmWithId);
        }
        return render("get_alarms_at.ftl", builder.build());
    }

    public String getAllAlarms(TimeRange timeRange, Integer afterAlarmWithId) {
        ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder()
                .put("fromMillis", timeRange.getStart())
                .put("toMillis", timeRange.getEnd())
                .put("maxBuckets", MAX_BUCKETS);
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
