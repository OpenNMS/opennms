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
 * 2008 Oct 04: Severity -> OnmsSeverity name change. - dj@opennms.org
 * 2008 Sep 27: Use new Severity enum. - dj@opennms.org
 * 2008 Aug 31: Update alarm list URL. - dj@opennms.org
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

import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.Alarm;
import org.opennms.web.alarm.AlarmFactory;
import org.opennms.web.alarm.SortStyle;
import org.opennms.web.alarm.filter.NodeFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.opennms.web.filter.Filter;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * 
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 */
public class AlarmFeed extends AbstractFeed {

    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Alarms");
        feed.setDescription("OpenNMS Alarms");
        feed.setLink(getUrlBase() + "alarm/list.htm");

        ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            Alarm[] alarms;

            ArrayList<Filter> filters = new ArrayList<Filter>();
            if (this.getRequest().getParameter("node") != null) {
                Integer nodeId = WebSecurityUtils.safeParseInt(this.getRequest().getParameter("node"));
                filters.add(new NodeFilter(nodeId));
            }
            if (this.getRequest().getParameter("severity") != null) {
                String sev = this.getRequest().getParameter("severity");
                for (OnmsSeverity severity : OnmsSeverity.values()) {
                    if (severity.getLabel().toLowerCase().equals(sev)) {
                        filters.add(new SeverityFilter(severity));
                    }
                }

            }
            
            alarms = AlarmFactory.getAlarms(SortStyle.FIRSTEVENTTIME, AcknowledgeType.BOTH, filters.toArray(new Filter[] {}), this.getMaxEntries(), -1);

            SyndEntry entry;
            
            for (Alarm alarm : alarms) {
                entry = new SyndEntryImpl();
                entry.setPublishedDate(alarm.getFirstEventTime());
                if (alarm.getAcknowledgeTime() != null) {
                    entry.setTitle(sanitizeTitle(alarm.getLogMessage()) + " (acknowledged by " + alarm.getAcknowledgeUser() + ")");
                    entry.setUpdatedDate(alarm.getAcknowledgeTime());
                } else {
                    entry.setTitle(sanitizeTitle(alarm.getLogMessage()));
                    entry.setUpdatedDate(alarm.getFirstEventTime());
                }
                entry.setLink(getUrlBase() + "alarm/detail.jsp?id=" + alarm.getId());
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            log().warn("unable to get event(s)", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
