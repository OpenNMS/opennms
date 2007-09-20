package org.opennms.web.rss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.opennms.web.outage.OutageModel;
import org.opennms.web.outage.OutageSummary;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

public class OutageFeed extends AbstractFeed {

    public OutageFeed() {
        super();
        // date-based
        setMaxEntries(Integer.MAX_VALUE);
    }
    
    public OutageFeed(String feedType) {
        super(feedType);
        // date-based
        setMaxEntries(Integer.MAX_VALUE);
    }
    
    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Nodes with Outages");
        feed.setDescription("OpenNMS Nodes with Outages");
        feed.setLink(getUrlBase() + "outage/current.jsp");

        ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            OutageModel model = new OutageModel();    
            Date date = new Date();
            date.setTime(date.getTime() - (1000 * 60 * 60 * 24));
            OutageSummary[] summaries = model.getAllOutageSummaries(date);

            SyndEntry entry;
            
            int count = 0;
            for (OutageSummary summary : summaries) {
                if (count++ == this.getMaxEntries()) {
                    break;
                }
                String link = getUrlBase() + "element/node.jsp?node=" + summary.getNodeId();

                entry = new SyndEntryImpl();
                entry.setPublishedDate(summary.getTimeDown());
                
                if (summary.getTimeUp() == null) {
                    entry.setTitle("outage: " + sanitizeTitle(summary.getNodeLabel()));
                    entry.setUpdatedDate(summary.getTimeDown());
                } else {
                    entry.setTitle("outage: " + sanitizeTitle(summary.getNodeLabel()) + " (resolved)");
                    entry.setUpdatedDate(summary.getTimeUp());
                }
                entry.setLink(link);
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            log().warn("unable to get current outages", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
