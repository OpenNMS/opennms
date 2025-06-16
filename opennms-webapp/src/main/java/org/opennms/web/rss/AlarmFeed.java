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

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.SortStyle;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.NodeFilter;
import org.opennms.web.alarm.filter.SeverityFilter;
import org.opennms.web.filter.Filter;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;

/**
 * <p>AlarmFeed class.</p>
 *
 * @author <a href="mailto:ranger@opennms.org">Ben Reed</a>
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 */
public class AlarmFeed extends AbstractFeed {

    private AlarmRepository m_webAlarmRepository;

    public AlarmFeed() {
        super();
        initialize();
    }

    public AlarmFeed(String feedType) {
        super(feedType);
        initialize();
    }

    private void initialize() {
        m_webAlarmRepository = BeanUtils.getBean("daoContext", "alarmRepository", AlarmRepository.class);
    }

    /**
     * <p>getFeed</p>
     *
     * @return a {@link com.rometools.rome.feed.synd.SyndFeed} object.
     */
    @Override
    public SyndFeed getFeed() {
        SyndFeed feed = new SyndFeedImpl();

        feed.setTitle("Alarms");
        feed.setDescription("OpenNMS Alarms");
        feed.setLink(getUrlBase() + "alarm/list.htm");

        List<SyndEntry> entries = new ArrayList<>();

        List<Filter> filters = new ArrayList<>();
        if (this.getRequest().getParameter("node") != null) {
            Integer nodeId = WebSecurityUtils.safeParseInt(this.getRequest().getParameter("node"));
            filters.add(new NodeFilter(nodeId, getServletContext()));
        }
        if (this.getRequest().getParameter("severity") != null) {
            String sev = this.getRequest().getParameter("severity");
            for (OnmsSeverity severity : OnmsSeverity.values()) {
                if (severity.getLabel().equalsIgnoreCase(sev)) {
                    filters.add(new SeverityFilter(severity));
                }
            }

        }

        OnmsCriteria queryCriteria = AlarmUtil.getOnmsCriteria(new AlarmCriteria(filters.toArray(new Filter[] {}), SortStyle.FIRSTEVENTTIME, AcknowledgeType.BOTH, this.getMaxEntries(), AlarmCriteria.NO_OFFSET));

        OnmsAlarm[] alarms = m_webAlarmRepository.getMatchingAlarms(queryCriteria);

        SyndEntry entry;
        
        for (OnmsAlarm alarm : alarms) {
            entry = new SyndEntryImpl();
            entry.setPublishedDate(alarm.getFirstEventTime());
            if (alarm.getAckTime() != null) {
                entry.setTitle(sanitizeTitle(alarm.getLogMsg()) + " (acknowledged by " + alarm.getAckUser() + ")");
                entry.setUpdatedDate(alarm.getAckTime());
            } else {
                entry.setTitle(sanitizeTitle(alarm.getLogMsg()));
                entry.setUpdatedDate(alarm.getFirstEventTime());
            }
            entry.setLink(getUrlBase() + "alarm/detail.htm?id=" + alarm.getId());
            entry.setAuthor("OpenNMS");
            
            SyndContent content = new SyndContentImpl();
            content.setType("text/html");
            content.setValue(alarm.getDescription());
            entry.setDescription(content);
            
            entries.add(entry);
        }

        feed.setEntries(entries);
        return feed;
    }

}
