package org.opennms.web.rss;

import java.sql.SQLException;
import java.util.ArrayList;

import org.opennms.web.notification.Notification;
import org.opennms.web.notification.NotificationModel;
import org.opennms.web.outage.OutageModel;
import org.opennms.web.outage.OutageSummary;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

public class NotificationFeed extends AbstractFeed {

    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Outstanding Notifications");
        feed.setDescription("Outstanding Notifications");
        feed.setLink(getUrlBase() + "notification/browse?acktype=unack");

        ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            NotificationModel model = new NotificationModel();
            Notification[] notifications = model.getOutstandingNotices();

            SyndEntry entry;
            
            int count = 0;
            for (Notification notification : notifications) {
                if (count++ == this.getMaxEntries()) {
                    break;
                }
                entry = new SyndEntryImpl();
                entry.setTitle(notification.getTextMessage());
                entry.setLink(getUrlBase() + "notification/detail.jsp?notice=" + notification.getId());
                entry.setPublishedDate(notification.getTimeSent());
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            log().warn("unable to get current outages", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
