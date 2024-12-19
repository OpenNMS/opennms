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
package org.opennms.netmgt.dao.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsEvent;

public interface EventDao extends LegacyOnmsDao<OnmsEvent, Integer> {

    int deletePreviousEventsForAlarm(final Integer id, final OnmsEvent e);

    /**
     * Returns a list of events which have been created
     * AFTER date and the uei of each event matches one uei entry of the ueiList.
     *
     * @param ueiList list with uei's
     * @param date    the date after which all events are loaded.
     * @return a list of events which have been created
     *         AFTER date and the uei of each event matches one uei entry of the ueiList.
     */
    List<OnmsEvent> getEventsAfterDate(List<String> ueiList, Date date);

    List<OnmsEvent> getEventsForEventParameters(final Map<String, String> eventParameters);

    int countNodesFromPast24Hours();
}
