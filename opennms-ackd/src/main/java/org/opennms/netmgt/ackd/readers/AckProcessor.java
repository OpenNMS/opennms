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
package org.opennms.netmgt.ackd.readers;

import org.springframework.beans.factory.InitializingBean;

/**
 * This class uses the JavaMail API to connect to a mail store and retrieve messages, using
 * the configured host and user details, and detects replies to notifications that have
 * an acknowledgment action: acknowledge, unacknowledge, clear, escalate.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public interface AckProcessor extends Runnable, InitializingBean {

    /**
     * <p>reloadConfigs</p>
     */
    public void reloadConfigs();
}
