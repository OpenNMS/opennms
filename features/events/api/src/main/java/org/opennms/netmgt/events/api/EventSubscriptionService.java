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
package org.opennms.netmgt.events.api;

import java.util.Collection;


/**
 * <p>EventSubscriptionService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EventSubscriptionService {
    
    /**
     * Registers an event listener that is interested in all events
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     */
    public void addEventListener(EventListener listener);

    /**
     * Registers an event listener interested in the UEIs in the passed list
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     * @param ueis a {@link java.util.Collection} object.
     */
    public void addEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Registers an event listener interested in the passed UEI
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void addEventListener(EventListener listener, String uei);

    /**
     * Removes a registered event listener
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     */
    public void removeEventListener(EventListener listener);

    /**
     * Removes a registered event listener - the UEI list indicates the list of
     * events the listener is no more interested in
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     * @param ueis a {@link java.util.Collection} object.
     */
    public void removeEventListener(EventListener listener, Collection<String> ueis);

    /**
     * Removes a registered event listener - the UEI indicates an event the
     * listener is no more interested in
     *
     * @param listener a {@link org.opennms.netmgt.events.api.EventListener} object.
     * @param uei a {@link java.lang.String} object.
     */
    public void removeEventListener(EventListener listener, String uei);

    /**
     * Checks if there is at least one listener for the given uei.
     *
     * @param uei the uie to check for
     *
     * @return {@code true} iff there is at least one listener
     */
    public boolean hasEventListener(final String uei);


}
