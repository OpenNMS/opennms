/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Fetch.FetchType;
import org.opennms.netmgt.dao.stats.AlarmStatisticsService;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * Basic Web Service using REST for NCS Components
 *
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 */
@Component
@PerRequest
@Scope("prototype")
@Path("stats/alarms")
@Transactional
public class AlarmStatsRestService extends AlarmRestServiceBase {
	
	private static final Logger LOG = LoggerFactory.getLogger(AlarmStatsRestService.class);


    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @Autowired
    AlarmStatisticsService m_statisticsService;

    @Context 
    UriInfo m_uriInfo;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public AlarmStatistics getStats() {
        readLock();
        try {
            return getStats(null);
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("/by-severity")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public AlarmStatisticsBySeverity getStatsForEachSeverity(@QueryParam("severities") final String severitiesString) {
        readLock();

        try {
            final AlarmStatisticsBySeverity stats = new AlarmStatisticsBySeverity();
    
            String[] severities = StringUtils.split(severitiesString, ",");
            if (severities == null || severities.length == 0) {
                severities = OnmsSeverity.names().toArray(EMPTY_STRING_ARRAY);
            }
    
            for (final String severityName : severities) {
                final OnmsSeverity severity = OnmsSeverity.get(severityName);
    
                final AlarmStatistics stat = getStats(severity);
                stat.setSeverity(severity);
                stats.add(stat);
            }
            
            return stats;
        } finally {
            readUnlock();
        }
    }
    
    protected AlarmStatistics getStats(final OnmsSeverity severity) {
        final AlarmStatistics stats = new AlarmStatistics();

        final CriteriaBuilder builder = getCriteriaBuilder(m_uriInfo.getQueryParameters(), false);

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

    protected CriteriaBuilder getCriteriaBuilder(final OnmsSeverity severity) {
    	final CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
        if (severity != null) {
            builder.eq("severity", severity);
        }

        builder.fetch("firstEvent", FetchType.EAGER);
        builder.fetch("lastEvent", FetchType.EAGER);
        
        builder.alias("node", "node", JoinType.LEFT_JOIN);
        builder.alias("node.snmpInterfaces", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("node.ipInterfaces", "ipInterface", JoinType.LEFT_JOIN);

        return builder;
    }

    @Entity
    @XmlRootElement(name = "severities")
    public static class AlarmStatisticsBySeverity {
        private List<AlarmStatistics> m_stats = new LinkedList<AlarmStatistics>();

        @XmlElement(name="alarmStatistics")
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
        public int getTotalCount() {
            return m_totalCount;
        }

        public void setTotalCount(final int count) {
            m_totalCount = count;
        }

        @XmlAttribute(name="acknowledgedCount")
        public int getAcknowledgedCount() {
            return m_acknowledgedCount;
        }
        
        public void setAcknowledgedCount(final int count) {
            m_acknowledgedCount = count;
        }

        @XmlAttribute(name="unacknowledgedCount")
        public int getUnacknowledgedCount() {
            return m_totalCount - m_acknowledgedCount;
        }
        
        public void setUnacknowledgedCount(final int count) {}

        @XmlAttribute(name="severity")
        public OnmsSeverity getSeverity() {
            return m_severity;
        }
        
        public void setSeverity(final OnmsSeverity severity) {
            m_severity = severity;
        }

        @XmlElementWrapper(name="newestAcked")
        @XmlElement(name="alarm")
        public List<OnmsAlarm> getNewestAcknowledged() {
            return Collections.singletonList(m_newestAcknowledged);
        }

        public void setNewestAcknowledged(final OnmsAlarm alarm) {
            m_newestAcknowledged = alarm;
        }

        @XmlElementWrapper(name="newestUnacked")
        @XmlElement(name="alarm")
        public List<OnmsAlarm> getNewestUnacknowledged() {
            return Collections.singletonList(m_newestUnacknowledged);
        }

        public void setNewestUnacknowledged(final OnmsAlarm alarm) {
            m_newestUnacknowledged = alarm;
        }

        @XmlElementWrapper(name="oldestAcked")
        @XmlElement(name="alarm")
        public List<OnmsAlarm> getOldestAcknowledged() {
            return Collections.singletonList(m_oldestAcknowledged);
        }

        public void setOldestAcknowledged(final OnmsAlarm alarm) {
            m_oldestAcknowledged = alarm;
        }

        @XmlElementWrapper(name="oldestUnacked")
        @XmlElement(name="alarm")
        public List<OnmsAlarm> getOldestUnacknowledged() {
            return Collections.singletonList(m_oldestUnacknowledged);
        }

        public void setOldestUnacknowledged(final OnmsAlarm alarm) {
            m_oldestUnacknowledged = alarm;
        }

    }

}
