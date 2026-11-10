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
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.web.outage.OutageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;

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
     * @return a {@link com.rometools.rome.feed.synd.SyndFeed} object.
     */
    @Override
    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Nodes with Outages");
        feed.setDescription("OpenNMS Nodes with Outages");
        feed.setLink(getUrlBase() + "outage/list.htm");

        List<SyndEntry> entries = new ArrayList<>();

        try {
            Date date = new Date();
            date.setTime(date.getTime() - (1000 * 60 * 60 * 24));
            OutageSummary[] summaries = OutageModel.getAllOutageSummaries(date);

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
                    entry.setTitle(sanitizeTitle(summary.getNodeLabel()) + " (Resolved)");
                    entry.setUpdatedDate(summary.getTimeUp());
                }
                entry.setLink(link);
                entry.setAuthor("OpenNMS");
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            LOG.warn("unable to get current outages", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
