package org.opennms.web.rss;

import java.sql.SQLException;
import java.util.ArrayList;

import org.opennms.web.notification.Notification;
import org.opennms.web.notification.NotificationModel;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

public class NotificationFeed extends AbstractFeed {

    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Notifications");
        feed.setDescription("Notifications");
        feed.setLink(getUrlBase() + "notification/browse");

        ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            NotificationModel model = new NotificationModel();
            Notification[] notifications = model.allNotifications();

            SyndEntry entry;
            
            int count = 0;
            for (Notification notification : notifications) {
                if (count++ == this.getMaxEntries()) {
                    break;
                }
                entry = new SyndEntryImpl();
                if (notification.getTimeReplied() == null) {
                    entry.setTitle(sanitizeTitle(notification.getTextMessage()));
                } else {
                    entry.setTitle(sanitizeTitle(notification.getTextMessage()) + " (acknowledged)");
                }
                entry.setLink(getUrlBase() + "notification/detail.jsp?notice=" + notification.getId());
                entry.setPublishedDate(notification.getTimeSent());
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            log().warn("unable to get outstanding notifications", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
