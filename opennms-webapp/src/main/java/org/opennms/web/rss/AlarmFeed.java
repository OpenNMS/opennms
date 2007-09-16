package org.opennms.web.rss;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.web.alarm.Alarm;
import org.opennms.web.alarm.AlarmFactory;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.AlarmFactory.AcknowledgeType;
import org.opennms.web.alarm.AlarmFactory.SortStyle;
import org.opennms.web.alarm.filter.Filter;
import org.opennms.web.alarm.filter.NodeFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.opennms.web.event.EventUtil;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

public class AlarmFeed extends AbstractFeed {

    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Alarms");
        feed.setDescription("OpenNMS Alarms");
        feed.setLink(getUrlBase() + "alarm/list");

        ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>();

        try {
            Alarm[] alarms;

            ArrayList<Filter> filters = new ArrayList<Filter>();
            if (this.getRequest().getParameter("node") != null) {
                Integer nodeId = Integer.parseInt(this.getRequest().getParameter("node"));
                filters.add(new NodeFilter(nodeId));
            }
            if (this.getRequest().getParameter("severity") != null) {
                String sev = this.getRequest().getParameter("severity");
                List<Integer> severities = AlarmUtil.getSeverityList();
                for (Integer severity : severities) {
                    if (EventUtil.getSeverityLabel(severity).toLowerCase().equals(sev)) {
                        filters.add(new SeverityFilter(severity));
                    }
                }

            }
            
            alarms = AlarmFactory.getAlarms(SortStyle.FIRSTEVENTTIME, AcknowledgeType.BOTH, filters.toArray(new Filter[] {}), this.getMaxEntries(), -1);

            SyndEntry entry;
            
            int count = 0;
            for (Alarm alarm : alarms) {
                if (count++ == this.getMaxEntries()) {
                    break;
                }
                entry = new SyndEntryImpl();
                if (alarm.getAcknowledgeTime() != null) {
                    entry.setTitle(sanitizeTitle(alarm.getLogMessage()) + " (acknowledged by " + alarm.getAcknowledgeUser() + ")");
                } else {
                    entry.setTitle(sanitizeTitle(alarm.getLogMessage()));
                }
                entry.setLink(getUrlBase() + "alarm/detail.jsp?id=" + alarm.getId());
                entry.setPublishedDate(alarm.getFirstEventTime());
                
                entries.add(entry);
            }
        } catch (SQLException e) {
            log().warn("unable to get event(s)", e);
        }
        
        feed.setEntries(entries);
        return feed;
    }

}
