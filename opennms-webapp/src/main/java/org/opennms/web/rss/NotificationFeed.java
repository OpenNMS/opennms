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

import org.opennms.web.notification.Notification;
import org.opennms.web.notification.NotificationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;

/**
 * <p>NotificationFeed class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class NotificationFeed extends AbstractFeed {
	
	private static final Logger LOG = LoggerFactory.getLogger(NotificationFeed.class);


    /**
     * <p>getFeed</p>
     *
     * @return a {@link com.rometools.rome.feed.synd.SyndFeed} object.
     */
    @Override
    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Notifications");
        feed.setDescription("Notifications");
        feed.setLink(getUrlBase() + "notification/browse");

        List<SyndEntry> entries = new ArrayList<>();

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
                entry.setAuthor("OpenNMS");
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            LOG.warn("unable to get outstanding notifications", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
