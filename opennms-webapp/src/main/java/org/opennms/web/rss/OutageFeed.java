/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
import java.util.Date;

import org.opennms.web.outage.OutageModel;
import org.opennms.web.outage.OutageSummary;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
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
        feed.setLink(getUrlBase() + "outage/list.htm");

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
                    entry.setTitle(sanitizeTitle(summary.getNodeLabel()));
                    entry.setUpdatedDate(summary.getTimeDown());
                } else {
                    entry.setTitle(sanitizeTitle(summary.getNodeLabel()) + " (resolved)");
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
