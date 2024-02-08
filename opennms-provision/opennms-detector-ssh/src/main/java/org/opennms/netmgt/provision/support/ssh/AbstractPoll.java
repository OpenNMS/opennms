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
package org.opennms.netmgt.provision.support.ssh;

import java.util.Collections;
import java.util.Map;

import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.PollStatus;

/**
 * <p>Abstract AbstractPoll class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractPoll implements Poll {
    // default timeout of 3 seconds
    protected int m_timeout = 3000;
    
    /**
     * Set the timeout in milliseconds.
     *
     * @param milliseconds the timeout
     */
    public void setTimeout(int milliseconds) {
        m_timeout = milliseconds;
    }

    /**
     * Get the timeout in milliseconds.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * <p>poll</p>
     *
     * @param tracker a {@link org.opennms.core.utils.TimeoutTracker} object.
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     * @throws org.opennms.netmgt.provision.support.ssh.InsufficientParametersException if any.
     */
    public abstract PollStatus poll(TimeoutTracker tracker) throws InsufficientParametersException;
    
    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     * @throws org.opennms.netmgt.provision.support.ssh.InsufficientParametersException if any.
     */
    @Override
    public PollStatus poll() throws InsufficientParametersException {
        Map<String,?> emptyMap = Collections.emptyMap();
        TimeoutTracker tracker = new TimeoutTracker(emptyMap, 1, getTimeout());
        return poll(tracker);
    }

}
