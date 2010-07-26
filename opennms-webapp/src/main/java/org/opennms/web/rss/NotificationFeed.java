/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
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

import org.opennms.web.notification.Notification;
import org.opennms.web.notification.NotificationModel;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * <p>NotificationFeed class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class NotificationFeed extends AbstractFeed {

    /**
     * <p>getFeed</p>
     *
     * @return a {@link com.sun.syndication.feed.synd.SyndFeed} object.
     */
    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Notifications");
        feed.setDescription("Notifications");
        feed.setLink(getUrlBase() + "notification/browse");

        ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            NotificationModel model = new NotificationModel();
            Notification[] notifications = model.allNotifications("desc");

            SyndEntry entry;
            
            int count = 0;
            for (Notification notification : notifications) {
                if (count++ == this.getMaxEntries()) {
                    break;
                }
                entry = new SyndEntryImpl();
                entry.setPublishedDate(notification.getTimeSent());
                if (notification.getTimeReplied() == null) {
                    entry.setTitle(sanitizeTitle(notification.getTextMessage()));
                    entry.setUpdatedDate(notification.getTimeSent());
                } else {
                    entry.setTitle(sanitizeTitle(notification.getTextMessage()) + " (acknowledged)");
                    entry.setUpdatedDate(notification.getTimeReplied());
                }
                entry.setLink(getUrlBase() + "notification/detail.jsp?notice=" + notification.getId());
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            log().warn("unable to get outstanding notifications", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
