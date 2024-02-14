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

import org.springframework.util.Assert;

/**
 * <p>EventIpcManagerFactory class.</p>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public abstract class EventIpcManagerFactory {
	/**
     * The EventIpcManager instance.
     */
    private static EventIpcManager m_ipcManager;

    /**
     * Create the singleton instance of this factory
     */
    public static synchronized void init() {
    }

    /**
     * Returns an implementation of the default EventIpcManager class
     *
     * @return a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public static EventIpcManager getIpcManager() {
        Assert.state(m_ipcManager != null, "this factory has not been initialized");
        return m_ipcManager;
    }

    /**
     * <p>setIpcManager</p>
     *
     * @param ipcManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public static void setIpcManager(EventIpcManager ipcManager) {
        Assert.notNull(ipcManager, "argument ipcManager must not be null");
        m_ipcManager = ipcManager;
    }
    
    /**
     * This is here for unit testing so we can reset this class before
     * every test.
     * 
     * @deprecated Only for unit testing!
     */
    public static void reset() {
        m_ipcManager = null;
    }

}
