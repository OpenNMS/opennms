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
package org.opennms.netmgt.config;

import java.util.List;

import org.opennms.netmgt.xml.event.Event;

/**
 * <p>EventTranslatorConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EventTranslatorConfig {

    static final String TRANSLATOR_NAME = "event-translator";

    /**
     * Get the list of UEIs that are registered in the passive status configuration.
     *
     * @return list of UEIs
     */
    List<String> getUEIList();

    /**
     * Translate the @param e to a new event
     *
     * @param e Event
     * @return a translated event
     */
    List<Event> translateEvent(Event e);

    /**
     * <p>update</p>
     *
     * @throws java.lang.Exception if any.
     */
    void update() throws Exception;

}
