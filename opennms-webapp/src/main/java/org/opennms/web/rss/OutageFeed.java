package org.opennms.web.rss;

import java.sql.SQLException;
import java.util.ArrayList;

import org.opennms.web.outage.OutageModel;
import org.opennms.web.outage.OutageSummary;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

public class OutageFeed extends AbstractFeed {

    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Nodes with Outages");
        feed.setDescription("OpenNMS Nodes with Outages");
        feed.setLink(getUrlBase() + "outage/current.jsp");

        ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            OutageModel model = new OutageModel();    
            OutageSummary[] summaries = model.getAllOutageSummaries();

            SyndEntry entry;
            
            int count = 0;
            for (OutageSummary summary : summaries) {
                if (count++ == this.getMaxEntries()) {
                    break;
                }
                entry = new SyndEntryImpl();
                if (summary.getTimeUp() == null) {
                    entry.setTitle("outage: " + sanitizeTitle(summary.getNodeLabel()));
                } else {
                    entry.setTitle("outage: " + sanitizeTitle(summary.getNodeLabel()) + " (resolved)");
                }
                entry.setLink(getUrlBase() + "element/node.jsp?node=" + summary.getNodeId());
                entry.setPublishedDate(summary.getTimeDown());
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            log().warn("unable to get current outages", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
