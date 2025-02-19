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
package org.opennms.systemreport.event;

import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class UserLoginEventListener implements EventListener {

    private EventSubscriptionService eventSubscriptionService;

    @Override
    public void onEvent(IEvent event) {
        if(EventConstants.AUTHENTICATION_SUCCESS_UEI.equals(event.getUei())) {

            String username = event.getParm("user").getValue().getContent();
            if (!username.equals("rtc")) {
                CsvUtils.logUserDataToCsv(username, event.getTime());
            }
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    public void init() {
        eventSubscriptionService.addEventListener(this);
    }

    public void destroy() {
        eventSubscriptionService.removeEventListener(this);
    }


    public EventSubscriptionService getEventSubscriptionService() {
        return eventSubscriptionService;
    }

    public void setEventSubscriptionService(EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

}
