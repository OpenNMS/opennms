/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.rss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.web.outage.OutageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * <p>OutageFeed class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class OutageFeed extends AbstractFeed {
	
	private static final Logger LOG = LoggerFactory.getLogger(OutageFeed.class);


    /**
     * <p>Constructor for OutageFeed.</p>
     */
    public OutageFeed() {
        super();
        // date-based
        setMaxEntries(Integer.MAX_VALUE);
    }
    
    /**
     * <p>Constructor for OutageFeed.</p>
     *
     * @param feedType a {@link java.lang.String} object.
     */
    public OutageFeed(String feedType) {
        super(feedType);
        // date-based
        setMaxEntries(Integer.MAX_VALUE);
    }
    
    /**
     * <p>getFeed</p>
     *
     * @return a {@link com.sun.syndication.feed.synd.SyndFeed} object.
     */
    @Override
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
            LOG.warn("unable to get current outages", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
