/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2009 Apr: refactoring to support ACL DAO work
 * Created: September 16, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.rss;

import java.sql.SQLException;
import java.util.ArrayList;

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.event.AcknowledgeType;
import org.opennms.web.event.Event;
import org.opennms.web.event.EventFactory;
import org.opennms.web.event.SortStyle;
import org.opennms.web.event.filter.NodeFilter;
import org.opennms.web.event.filter.SeverityFilter;
import org.opennms.web.filter.Filter;

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

    /**
     * <p>getFeed</p>
     *
     * @return a {@link com.sun.syndication.feed.synd.SyndFeed} object.
     */
    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Events");
        feed.setDescription("OpenNMS Events");
        feed.setLink(getUrlBase() + "event/list.htm");

        ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            Event[] events;

            ArrayList<Filter> filters = new ArrayList<Filter>();
            if (this.getRequest().getParameter("node") != null) {
                Integer nodeId = WebSecurityUtils.safeParseInt(this.getRequest().getParameter("node"));
                filters.add(new NodeFilter(nodeId));
            }
            if (this.getRequest().getParameter("severity") != null) {
                String parameter = this.getRequest().getParameter("severity");
                try {
                    Integer severityId = WebSecurityUtils.safeParseInt(parameter);
                    filters.add(new SeverityFilter(severityId));
                } catch (NumberFormatException e) {
                    for (OnmsSeverity sev : OnmsSeverity.values()) {
                        if (sev.getLabel().toLowerCase().equals(parameter.toLowerCase())) {
                            filters.add(new SeverityFilter(sev));
                            break;
                        }
                    }
                }
            }
            
            events = EventFactory.getEvents(SortStyle.TIME, AcknowledgeType.BOTH, filters.toArray(new Filter[] {}), this.getMaxEntries(), -1);

            SyndEntry entry;
            
            for (Event event : events) {
                entry = new SyndEntryImpl();
                entry.setPublishedDate(event.getTime());
                if (event.getAcknowledgeTime() != null) {
                    entry.setTitle(sanitizeTitle(event.getLogMessage()) + " (acknowledged by " + event.getAcknowledgeUser() + ")");
                    entry.setUpdatedDate(event.getAcknowledgeTime());
                } else {
                    entry.setTitle(sanitizeTitle(event.getLogMessage()));
                    entry.setUpdatedDate(event.getTime());
                }
                entry.setLink(getUrlBase() + "event/detail.jsp?id=" + event.getId());
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            log().warn("unable to get event(s)", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
