/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rss;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.SortStyle;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.NodeFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.opennms.web.filter.Filter;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * <p>AlarmFeed class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 */
public class AlarmFeed extends AbstractFeed {

    private AlarmRepository m_webAlarmRepository;

    public AlarmFeed() {
        super();
        initialize();
    }

    public AlarmFeed(String feedType) {
        super(feedType);
        initialize();
    }

    private void initialize() {
        m_webAlarmRepository = BeanUtils.getBean("daoContext", "alarmRepository", AlarmRepository.class);
    }

    /**
     * <p>getFeed</p>
     *
     * @return a {@link com.sun.syndication.feed.synd.SyndFeed} object.
     */
    @Override
    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Alarms");
        feed.setDescription("OpenNMS Alarms");
        feed.setLink(getUrlBase() + "alarm/list.htm");

        List<SyndEntry> entries = new ArrayList<SyndEntry>();

        List<Filter> filters = new ArrayList<Filter>();
        if (this.getRequest().getParameter("node") != null) {
            Integer nodeId = WebSecurityUtils.safeParseInt(this.getRequest().getParameter("node"));
            filters.add(new NodeFilter(nodeId, getServletContext()));
        }
        if (this.getRequest().getParameter("severity") != null) {
            String sev = this.getRequest().getParameter("severity");
            for (OnmsSeverity severity : OnmsSeverity.values()) {
                if (severity.getLabel().equalsIgnoreCase(sev)) {
                    filters.add(new SeverityFilter(severity));
                }
            }

        }

        OnmsCriteria queryCriteria = AlarmUtil.getOnmsCriteria(new AlarmCriteria(filters.toArray(new Filter[] {}), SortStyle.FIRSTEVENTTIME, AcknowledgeType.BOTH, this.getMaxEntries(), AlarmCriteria.NO_OFFSET));

        OnmsAlarm[] alarms = m_webAlarmRepository.getMatchingAlarms(queryCriteria);

        SyndEntry entry;
        
        for (OnmsAlarm alarm : alarms) {
            entry = new SyndEntryImpl();
            entry.setPublishedDate(alarm.getFirstEventTime());
            if (alarm.getAckTime() != null) {
                entry.setTitle(sanitizeTitle(alarm.getLogMsg()) + " (acknowledged by " + alarm.getAckUser() + ")");
                entry.setUpdatedDate(alarm.getAckTime());
            } else {
                entry.setTitle(sanitizeTitle(alarm.getLogMsg()));
                entry.setUpdatedDate(alarm.getFirstEventTime());
            }
            entry.setLink(getUrlBase() + "alarm/detail.htm?id=" + alarm.getId());
            entry.setAuthor("OpenNMS");
            
            SyndContent content = new SyndContentImpl();
            content.setType("text/html");
            content.setValue(alarm.getDescription());
            entry.setDescription(content);
            
            entries.add(entry);
        }

        feed.setEntries(entries);
        return feed;
    }

}
