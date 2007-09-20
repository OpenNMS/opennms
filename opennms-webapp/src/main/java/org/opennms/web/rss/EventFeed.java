package org.opennms.web.rss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.web.event.Event;
import org.opennms.web.event.EventFactory;
import org.opennms.web.event.EventUtil;
import org.opennms.web.event.EventFactory.AcknowledgeType;
import org.opennms.web.event.EventFactory.SortStyle;
import org.opennms.web.event.filter.Filter;
import org.opennms.web.event.filter.NodeFilter;
import org.opennms.web.event.filter.SeverityFilter;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

public class EventFeed extends AbstractFeed {

    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Events");
        feed.setDescription("OpenNMS Events");
        feed.setLink(getUrlBase() + "event/list");

        ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            Event[] events;

            ArrayList<Filter> filters = new ArrayList<Filter>();
            if (this.getRequest().getParameter("node") != null) {
                Integer nodeId = Integer.parseInt(this.getRequest().getParameter("node"));
                filters.add(new NodeFilter(nodeId));
            }
            if (this.getRequest().getParameter("severity") != null) {
                String sev = this.getRequest().getParameter("severity");
                List<Integer> severities = EventUtil.getSeverityList();
                for (Integer severity : severities) {
                    if (EventUtil.getSeverityLabel(severity).toLowerCase().equals(sev)) {
                        filters.add(new SeverityFilter(severity));
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
