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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.event.AcknowledgeType;
import org.opennms.web.event.Event;
import org.opennms.web.event.EventFactory;
import org.opennms.web.event.SortStyle;
import org.opennms.web.event.filter.NodeFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;


/**
 * <p>EventFeed class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class EventFeed extends AbstractFeed {
	
	private static final Logger LOG = LoggerFactory.getLogger(EventFeed.class);


    /**
     * <p>getFeed</p>
     *
     * @return a {@link com.sun.syndication.feed.synd.SyndFeed} object.
     */
    @Override
    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Events");
        feed.setDescription("OpenNMS Events");
        feed.setLink(getUrlBase() + "event/list.htm");

        List<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            Event[] events;

            ArrayList<Filter> filters = new ArrayList<Filter>();
            if (this.getRequest().getParameter("node") != null) {
                Integer nodeId = WebSecurityUtils.safeParseInt(this.getRequest().getParameter("node"));
                filters.add(new NodeFilter(nodeId, getServletContext()));
            }
            if (this.getRequest().getParameter("severity") != null) {
                String parameter = this.getRequest().getParameter("severity");
                try {
                    Integer severityId = WebSecurityUtils.safeParseInt(parameter);
                    filters.add(new SeverityFilter(severityId));
                } catch (NumberFormatException e) {
                    for (OnmsSeverity sev : OnmsSeverity.values()) {
                        if (sev.getLabel().equalsIgnoreCase(parameter)) {
                            filters.add(new SeverityFilter(sev));
                            break;
                        }
                    }
                }
            }
            
            events = EventFactory.getEvents(SortStyle.TIME, AcknowledgeType.BOTH, filters.toArray(new Filter[] {}), this.getMaxEntries(), 0);

            SyndEntry entry;
            
            for (Event event : events) {
                entry = new SyndEntryImpl();
                entry.setPublishedDate(event.getTime());
                if (event.getAcknowledgeTime() != null) {
                    entry.setTitle(sanitizeTitle(event.getLogMessage()) + " (Acknowledged by " + event.getAcknowledgeUser() + ")");
                    entry.setUpdatedDate(event.getAcknowledgeTime());
                } else {
                    entry.setTitle(sanitizeTitle(event.getLogMessage()));
                    entry.setUpdatedDate(event.getTime());
                }
                entry.setLink(getUrlBase() + "event/detail.jsp?id=" + event.getId());
                entry.setAuthor("OpenNMS");
                
                SyndContent content = new SyndContentImpl();
                content.setType("text/html");
                content.setValue(event.getDescription());
                entry.setDescription(content);
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            LOG.warn("unable to get event(s)", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
