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
package org.opennms.netmgt.events.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventCollection;

import com.google.common.base.Strings;

@Command(scope = "opennms", name = "event-list", description = "Lists historical events meeting certain criteria")
@Service
public class EventListCommand implements Action {

    @Reference
    public EventConfDao eventConfDao;
    
    @Reference
    public EventDao eventDao;

    @Option(name="-l", aliases="--limit", description="Limit the number of events that are shown.")
    int limit = 10;

    @Argument(name="uei", description="Event UEI to match (exact).", required = false, multiValued = false)
    @Completion(EventUeiCompleter.class)
    String eventUeiMatch;

    @Override
    public Object execute() {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsEvent.class)
                .orderBy("eventTime").desc()
                .limit(limit)
                .alias("node",  "node", JoinType.LEFT_JOIN)
                .alias("serviceType", "serviceType", JoinType.LEFT_JOIN);
        
        if (! Strings.isNullOrEmpty(eventUeiMatch)) {
            criteriaBuilder.eq("eventUei", eventUeiMatch);
        }

        final OnmsEventCollection eventCollection = new OnmsEventCollection(eventDao.findMatching(criteriaBuilder.toCriteria()));
        eventCollection.setTotalCount(eventDao.countMatching(criteriaBuilder.toCriteria()));
        System.out.println(String.format("Found %d events, showing %d:", eventCollection.getTotalCount(), eventCollection.size()));
        try {
            ShellTable table = new ShellTable();
            table.column("ID");
            table.column("UEI");
            table.column("Severity");
            table.column("Time");
            table.column("Node Label");
            table.column("Interface");
            table.column("Service");
            for (OnmsEvent event : eventCollection) {
                String ipAddr = "";
                if (event.getIpAddr() != null) {
                    ipAddr = event.getIpAddr().getHostAddress();
                }
                String serviceName = "";
                if (event.getServiceType() != null) {
                    serviceName = event.getServiceType().getName();
                }
                table.addRow().addContent(event.getId(), event.getEventUei(), event.getSeverityLabel(), event.getEventTime(), event.getNodeLabel(), ipAddr, serviceName);
            }
            table.print(System.out);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }

        return null;
    }
}
