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
package org.opennms.web.svclayer;

import java.util.List;

import org.opennms.reporting.core.svclayer.DeliveryConfig;
import org.opennms.reporting.core.svclayer.ScheduleConfig;
import org.opennms.web.svclayer.model.TriggerDescription;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>SchedulerService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional(readOnly = true)
public interface SchedulerService {

    List<TriggerDescription> getTriggerDescriptions();

    @Transactional
    void removeTrigger(String triggerName);

    @Transactional
    void removeTriggers(String[] triggerNames);
    
    Boolean exists(String triggerName);

    @Transactional
    void updateCronTrigger(String cronTrigger, ScheduleConfig scheduleConfig);

    @Transactional
    void addCronTrigger(ScheduleConfig scheduleConfig);

    @Transactional
    void execute(DeliveryConfig deliveryConfig);

}
