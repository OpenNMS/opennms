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

import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

/**
 * <p>TroubleTicketProxy interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Transactional
public interface TroubleTicketProxy {

    /**
     * <p>createTicket</p>
     *
     * @param alarmId a {@link java.lang.Integer} object.
     */
    public void createTicket(Integer alarmId, Map<String,String> attributes);
    
    /**
     * <p>updateTicket</p>
     *
     * @param alarmId a {@link java.lang.Integer} object.
     */
    public void updateTicket(Integer alarmId);
    
    /**
     * <p>closeTicket</p>
     *
     * @param alarmId a {@link java.lang.Integer} object.
     */
    public void closeTicket(Integer alarmId);
}
