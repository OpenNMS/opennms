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

import org.opennms.netmgt.model.OnmsDistPoller;

/**
 * <p>DistPollerDao interface.</p>
 */
public interface DistPollerDao extends OnmsDao<OnmsDistPoller, String> {

    /**
     * This function returns the {@link OnmsDistPoller} identity of the
     * local system so that events and other objects can be associated with
     * the local system.
     * 
     * @see https://en.wikipedia.org/wiki/Whoami
     * 
     * @return
     */
    OnmsDistPoller whoami();

}
