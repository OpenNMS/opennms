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
package org.opennms.web.rest.v2;

import static org.opennms.netmgt.events.api.EventConstants.APPLICATION_CHANGED_EVENT_UEI;
import static org.opennms.netmgt.events.api.EventConstants.PARM_APPLICATION_ID;
import static org.opennms.netmgt.events.api.EventConstants.PARM_APPLICATION_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class ApplicationEventUtil {

    public static List<Event> getApplicationChangedEvents(final Collection<OnmsApplication> applications) {
        List<Event> events = new ArrayList<>();
        if(applications == null) {
            return events;
        }
        for(OnmsApplication application : applications) {
            final Event event = new EventBuilder(APPLICATION_CHANGED_EVENT_UEI, "ReST")
                    .addParam(PARM_APPLICATION_ID, application.getId())
                    .addParam(PARM_APPLICATION_NAME, application.getName())
                    .getEvent();
            events.add(event);
        }
        return events;
    }
}
