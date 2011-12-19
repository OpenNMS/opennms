/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.dao.stats.AlarmStatisticsService;
import org.opennms.netmgt.dao.stats.StatisticsContainer;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
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

    // private static Logger s_log = LoggerFactory.getLogger(AlarmStatsRestService.class);

	@Autowired
	AlarmStatisticsService m_statisticsService;

    @Context 
    UriInfo m_uriInfo;

    @GET
    public AlarmStatisticsList getStats() {
        final AlarmStatisticsList stats = new AlarmStatisticsList();

        final OnmsCriteria queryFilters = getQueryFilters(m_uriInfo.getQueryParameters(), true);

        final int count = m_statisticsService.getTotalCount(queryFilters);
        stats.setCount(count);
        stats.setTotalCount(count);
        stats.setAcknowledgedCount(m_statisticsService.getAcknowledgedCount(queryFilters));

        stats.setNewestAcknowledged(getNewestAcknowledged());
        stats.setNewestUnacknowledged(getNewestUnacknowledged());
        stats.setOldestAcknowledged(getOldestAcknowledged());
        stats.setOldestUnacknowledged(getOldestUnacknowledged());

        return stats;
    }

    protected OnmsAlarm getNewestAcknowledged() {
        final MultivaluedMap<String,String> parameters = m_uriInfo.getQueryParameters();
        translateSeverity(m_uriInfo.getQueryParameters());
        parameters.putSingle("orderBy", "lastEventTime");
        parameters.putSingle("order", "desc");
        parameters.putSingle("limit", "1");
        return m_statisticsService.getAcknowledged(getQueryFilters(parameters));
    }

    private OnmsAlarm getNewestUnacknowledged() {
        final MultivaluedMap<String,String> parameters = m_uriInfo.getQueryParameters();
        translateSeverity(m_uriInfo.getQueryParameters());
        parameters.putSingle("orderBy", "lastEventTime");
        parameters.putSingle("order", "desc");
        parameters.putSingle("limit", "1");
        return m_statisticsService.getUnacknowledged(getQueryFilters(parameters));
    }

    protected OnmsAlarm getOldestAcknowledged() {
        final MultivaluedMap<String,String> parameters = m_uriInfo.getQueryParameters();
        translateSeverity(m_uriInfo.getQueryParameters());
        parameters.putSingle("orderBy", "lastEventTime");
        parameters.putSingle("order", "ast");
        parameters.putSingle("limit", "1");
        return m_statisticsService.getAcknowledged(getQueryFilters(parameters));
    }

    private OnmsAlarm getOldestUnacknowledged() {
        final MultivaluedMap<String,String> parameters = m_uriInfo.getQueryParameters();
        translateSeverity(m_uriInfo.getQueryParameters());
        parameters.putSingle("orderBy", "lastEventTime");
        parameters.putSingle("order", "ast");
        parameters.putSingle("limit", "1");
        return m_statisticsService.getUnacknowledged(getQueryFilters(parameters));
    }

    @Entity
    @XmlRootElement(name = "alarms")
    public static class AlarmStatisticsList extends LinkedList<StatisticsContainer> {
        private static final long serialVersionUID = 5956738313189529009L;

        private int m_totalCount = 0;
        private int m_acknowledgedCount = 0;
        private OnmsAlarm m_newestAcknowledged;
        private OnmsAlarm m_newestUnacknowledged;
        private OnmsAlarm m_oldestAcknowledged;
        private OnmsAlarm m_oldestUnacknowledged;

        @XmlAttribute(name="count")
        public int getCount() {
            return this.size();
        }

        // The property has a getter "" but no setter. For unmarshalling, please define setters.
        public void setCount(final int count) {}

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
