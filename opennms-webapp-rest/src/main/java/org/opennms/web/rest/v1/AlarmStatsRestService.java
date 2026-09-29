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
package org.opennms.web.rest.v1;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.netmgt.dao.api.AlarmStatisticsService;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for NCS Components
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
@Component("alarmStatsRestService")
@Path("stats/alarms")
@Tag(name = "Alarms Stats", description = "Alarms Stats API")
@Transactional
public class AlarmStatsRestService extends AlarmRestServiceBase {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmStatsRestService.class);

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @Autowired
    private AlarmStatisticsService m_statisticsService;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get alarm statistics",
            description = """
                    Return acknowledged and unacknowledged alarm counts together with the newest and oldest alarm
                    in each of those two sets.
                    Query parameters are applied as alarm filters in the same way as `GET /alarms`. The four
                    newest/oldest lookups run against their own criteria and are not filtered by those parameters.
                    In JSON each of `newestAcked`, `newestUnacked`, `oldestAcked` and `oldestUnacked` is an array
                    of one element, and that element is `null` when the set is empty. Alarm timestamps inside them
                    are epoch milliseconds in JSON and ISO-8601 strings in XML.""",
            operationId = "getAlarmStatsV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The statistics. `severity` is null on this operation.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AlarmStatistics.class),
                            examples = @ExampleObject(value = """
                    {
                      "acknowledgedCount": 2,
                      "unacknowledgedCount": 14,
                      "totalCount": 16,
                      "severity": null,
                      "newestAcked": [
                        {
                          "id": 4548,
                          "uei": "uei.opennms.org/apidoc/validationAlarm",
                          "severity": "WARNING",
                          "ackUser": "admin",
                          "ackTime": 1787727618396,
                          "firstEventTime": 1787727549288,
                          "lastEventTime": 1787727549288,
                          "count": 1,
                          "type": 3,
                          "parameters": []
                        }
                      ],
                      "newestUnacked": [ null ],
                      "oldestAcked": [ null ],
                      "oldestUnacked": [ null ]
                    }"""))),
            @ApiResponse(responseCode = "500", description = "A query parameter is not a property of the alarm entity.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null")))
    })
    public AlarmStatistics getStats(@Context final UriInfo uriInfo) {
        return getStats(uriInfo, null);
    }

    @GET
    @Path("/by-severity")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Operation(
            summary = "Get alarm statistics per severity",
            description = """
                    Return one statistics block per severity. With `severities` absent, all seven severities are
                    reported in ordinal order.
                    A name that is not a severity resolves to `INDETERMINATE` rather than being rejected. Names
                    are matched case-insensitively.
                    Other query parameters are applied as alarm filters, as on `GET /stats/alarms`.""",
            operationId = "getAlarmStatsBySeverityV1"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "One statistics block per requested severity.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = AlarmStatisticsBySeverity.class),
                            examples = @ExampleObject(value = """
                    {
                      "alarmStatistics": [
                        {
                          "acknowledgedCount": 0,
                          "unacknowledgedCount": 3,
                          "totalCount": 3,
                          "severity": "MINOR",
                          "newestAcked": [ null ],
                          "newestUnacked": [ null ],
                          "oldestAcked": [ null ],
                          "oldestUnacked": [ null ]
                        },
                        {
                          "acknowledgedCount": 0,
                          "unacknowledgedCount": 3,
                          "totalCount": 3,
                          "severity": "MAJOR",
                          "newestAcked": [ null ],
                          "newestUnacked": [ null ],
                          "oldestAcked": [ null ],
                          "oldestUnacked": [ null ]
                        }
                      ]
                    }"""))),
            @ApiResponse(responseCode = "500", description = "A query parameter is not a property of the alarm entity.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "Unknown entity: null; nested exception is org.hibernate.HibernateException: Unknown entity: null")))
    })
    public AlarmStatisticsBySeverity getStatsForEachSeverity(@Context final UriInfo uriInfo,
            @Parameter(description = "Comma-separated severity names to report on. All severities when omitted.",
                    example = "MINOR,MAJOR",
                    schema = @Schema(type = "string"))
            @QueryParam("severities") final String severitiesString) {
        final AlarmStatisticsBySeverity stats = new AlarmStatisticsBySeverity();

        String[] severities = StringUtils.split(severitiesString, ",");
        if (severities == null || severities.length == 0) {
            severities = OnmsSeverity.names().toArray(EMPTY_STRING_ARRAY);
        }

        for (final String severityName : severities) {
            final OnmsSeverity severity = OnmsSeverity.get(severityName);

            final AlarmStatistics stat = getStats(uriInfo, severity);
            stat.setSeverity(severity);
            stats.add(stat);
        }
        
        return stats;
    }

    protected AlarmStatistics getStats(final UriInfo uriInfo, final OnmsSeverity severity) {
        final AlarmStatistics stats = new AlarmStatistics();

        final CriteriaBuilder builder = getCriteriaBuilder(uriInfo.getQueryParameters(), false);

        // note: this is just the *total count* criteria, so no ordering, and count everything
        builder.count();

        if (severity != null) {
            builder.eq("severity", severity);
        }

        final Criteria criteria = builder.toCriteria();
        
        LOG.debug("criteria = {}", criteria);

        final int count = m_statisticsService.getTotalCount(criteria);
        stats.setTotalCount(count);
        stats.setAcknowledgedCount(m_statisticsService.getAcknowledgedCount(criteria));

        stats.setNewestAcknowledged(getNewestAcknowledged(severity));
        stats.setNewestUnacknowledged(getNewestUnacknowledged(severity));
        stats.setOldestAcknowledged(getOldestAcknowledged(severity));
        stats.setOldestUnacknowledged(getOldestUnacknowledged(severity));

        return stats;
    }

    protected OnmsAlarm getNewestAcknowledged(final OnmsSeverity severity) {
        final CriteriaBuilder builder = getCriteriaBuilder(severity);
        builder.orderBy("lastEventTime").desc();
        builder.orderBy("id").desc();
        builder.limit(1);
        final Criteria criteria = builder.toCriteria();
        LOG.debug("getNewestAcknowledged({}) criteria = {}", severity, criteria);
        return m_statisticsService.getAcknowledged(criteria);
    }

    private OnmsAlarm getNewestUnacknowledged(final OnmsSeverity severity) {
        final CriteriaBuilder builder = getCriteriaBuilder(severity);
        builder.orderBy("lastEventTime").desc();
        builder.orderBy("id").desc();
        builder.limit(1);
        final Criteria criteria = builder.toCriteria();
        LOG.debug("getNewestUnacknowledged({}) criteria = {}", severity, criteria);
        return m_statisticsService.getUnacknowledged(criteria);
    }

    protected OnmsAlarm getOldestAcknowledged(final OnmsSeverity severity) {
        final CriteriaBuilder builder = getCriteriaBuilder(severity);
        builder.orderBy("firstEventTime").asc();
        builder.orderBy("id").asc();
        builder.limit(1);
        final Criteria criteria = builder.toCriteria();
        LOG.debug("getOldestAcknowledged({}) criteria = {}", severity, criteria);
        return m_statisticsService.getAcknowledged(criteria);
    }

    private OnmsAlarm getOldestUnacknowledged(final OnmsSeverity severity) {
        final CriteriaBuilder builder = getCriteriaBuilder(severity);
        builder.orderBy("firstEventTime").asc();
        builder.orderBy("id").asc();
        builder.limit(1);
        final Criteria criteria = builder.toCriteria();
        LOG.debug("getOldestUnacknowledged({}) criteria = {}", severity, criteria);
        return m_statisticsService.getUnacknowledged(criteria);
    }

    protected static CriteriaBuilder getCriteriaBuilder(final OnmsSeverity severity) {
    	final CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
        if (severity != null) {
            builder.eq("severity", severity);
        }

        builder.fetch("lastEvent", FetchType.EAGER);
        
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);

        return builder;
    }

    @Entity
    @XmlRootElement(name = "severities")
    @Schema(name = "AlarmStatisticsBySeverity", description = "One alarm statistics block per severity.")
    public static class AlarmStatisticsBySeverity {
        private List<AlarmStatistics> m_stats = new LinkedList<>();

        @XmlElement(name="alarmStatistics")
        @Schema(description = "One entry per requested severity, in the order they were requested.")
        public List<AlarmStatistics> getStats() {
            return m_stats;
        }

        public void setStats(final List<AlarmStatistics> stats) {
            m_stats = stats;
        }
        
        public void add(final AlarmStatistics stats) {
            m_stats.add(stats);
        }
        
        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("alarmStatistics", m_stats)
                .toString();
        }
    }
    
    @Entity
    @XmlRootElement(name = "alarmStatistics")
    @Schema(name = "AlarmStatistics", description = "Acknowledged and unacknowledged alarm counts, plus the newest "
            + "and oldest alarm in each set.")
    public static class AlarmStatistics {
        private int m_totalCount = 0;
        private int m_acknowledgedCount = 0;
        private OnmsSeverity m_severity = null;

        private OnmsAlarm m_newestAcknowledged;
        private OnmsAlarm m_newestUnacknowledged;
        private OnmsAlarm m_oldestAcknowledged;
        private OnmsAlarm m_oldestUnacknowledged;

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                .append("totalCount", m_totalCount)
                .append("acknowledgedCount", m_acknowledgedCount)
                .append("unacknowledgedCount", getUnacknowledgedCount())
                .append("newestAcknowledged", m_newestAcknowledged)
                .append("newestUnacknowledged", m_newestUnacknowledged)
                .append("oldestAcknowledged", m_oldestAcknowledged)
                .append("oldestUnacknowledged", m_oldestUnacknowledged)
                .toString();
        }
        @XmlAttribute(name="totalCount")
        @Schema(description = "Alarms matching the criteria, acknowledged or not.", example = "16")
        public int getTotalCount() {
            return m_totalCount;
        }

        public void setTotalCount(final int count) {
            m_totalCount = count;
        }

        @XmlAttribute(name="acknowledgedCount")
        @Schema(description = "Matching alarms that carry an acknowledgement.", example = "2")
        public int getAcknowledgedCount() {
            return m_acknowledgedCount;
        }
        
        public void setAcknowledgedCount(final int count) {
            m_acknowledgedCount = count;
        }

        @XmlAttribute(name="unacknowledgedCount")
        @Schema(description = "Derived as `totalCount` minus `acknowledgedCount`; the setter is a no-op.",
                example = "14")
        public int getUnacknowledgedCount() {
            return m_totalCount - m_acknowledgedCount;
        }
        
        public void setUnacknowledgedCount(final int count) {}

        @XmlAttribute(name="severity")
        @Schema(description = "Severity this block covers. Null on `GET /stats/alarms`.", example = "MINOR")
        public OnmsSeverity getSeverity() {
            return m_severity;
        }
        
        public void setSeverity(final OnmsSeverity severity) {
            m_severity = severity;
        }

        @XmlElementWrapper(name="newestAcked")
        @XmlElement(name="alarm")
        @Schema(description = "Most recent acknowledged alarm, as a one-element list whose element is null when "
                + "there is none.")
        public List<OnmsAlarm> getNewestAcknowledged() {
            return Collections.singletonList(m_newestAcknowledged);
        }

        public void setNewestAcknowledged(final OnmsAlarm alarm) {
            m_newestAcknowledged = alarm;
        }

        @XmlElementWrapper(name="newestUnacked")
        @XmlElement(name="alarm")
        @Schema(description = "Most recent unacknowledged alarm, as a one-element list whose element is null when "
                + "there is none.")
        public List<OnmsAlarm> getNewestUnacknowledged() {
            return Collections.singletonList(m_newestUnacknowledged);
        }

        public void setNewestUnacknowledged(final OnmsAlarm alarm) {
            m_newestUnacknowledged = alarm;
        }

        @XmlElementWrapper(name="oldestAcked")
        @XmlElement(name="alarm")
        @Schema(description = "Oldest acknowledged alarm by `firstEventTime`, as a one-element list whose element "
                + "is null when there is none.")
        public List<OnmsAlarm> getOldestAcknowledged() {
            return Collections.singletonList(m_oldestAcknowledged);
        }

        public void setOldestAcknowledged(final OnmsAlarm alarm) {
            m_oldestAcknowledged = alarm;
        }

        @XmlElementWrapper(name="oldestUnacked")
        @XmlElement(name="alarm")
        @Schema(description = "Oldest unacknowledged alarm by `firstEventTime`, as a one-element list whose "
                + "element is null when there is none.")
        public List<OnmsAlarm> getOldestUnacknowledged() {
            return Collections.singletonList(m_oldestUnacknowledged);
        }

        public void setOldestUnacknowledged(final OnmsAlarm alarm) {
            m_oldestUnacknowledged = alarm;
        }

    }

}
