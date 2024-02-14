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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;

import static org.opennms.core.health.api.HealthCheckConstants.ELASTIC;

/**
 * Verifies the basic functionality (+ connectivity to Elasticsearch) of
 * the {@link ElasticAlarmHistoryRepository}.
 *
 * @author mvrueden
 */
public class ElasticHealthCheck implements HealthCheck {

    private final ElasticAlarmHistoryRepository elasticAlarmHistoryRepository;

    public ElasticHealthCheck(ElasticAlarmHistoryRepository elasticAlarmHistoryRepository) {
        this.elasticAlarmHistoryRepository = Objects.requireNonNull(elasticAlarmHistoryRepository);
    }

    @Override
    public String getDescription() {
        return "Number of active alarms stored in Elasticsearch (Alarm History)";
    }

    @Override
    public List<String> getTags() {
        return Arrays.asList(ELASTIC);
    }

    @Override
    public Response perform(Context context) {
        final long numAlarms = elasticAlarmHistoryRepository.getNumActiveAlarmsNow();
        // Any value is OK - a runtime exception is a failure
        return new Response(Status.Success, String.format("Found %d alarms.", numAlarms));
    }
}
