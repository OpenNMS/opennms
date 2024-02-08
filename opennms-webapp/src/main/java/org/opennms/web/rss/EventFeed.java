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
package org.opennms.web.rss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;


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
     * @return a {@link com.rometools.rome.feed.synd.SyndFeed} object.
     */
    @Override
    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Events");
        feed.setDescription("OpenNMS Events");
        feed.setLink(getUrlBase() + "event/list.htm");

        List<SyndEntry> entries = new ArrayList<>();

        try {
            Event[] events;

            ArrayList<Filter> filters = new ArrayList<>();
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
