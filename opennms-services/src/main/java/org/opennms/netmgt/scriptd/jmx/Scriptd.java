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
package org.opennms.netmgt.scriptd.jmx;

import org.apache.commons.lang.NotImplementedException;

/**
 * <p>Scriptd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Scriptd implements ScriptdMBean {
    /**
     * <p>init</p>
     */
    @Override
    public void init() {
        org.opennms.netmgt.scriptd.Scriptd scriptd = org.opennms.netmgt.scriptd.Scriptd.getInstance();
        scriptd.init();
    }

    /**
     * <p>start</p>
     */
    @Override
    public void start() {
        org.opennms.netmgt.scriptd.Scriptd scriptd = org.opennms.netmgt.scriptd.Scriptd.getInstance();
        scriptd.start();
    }

    /**
     * <p>stop</p>
     */
    @Override
    public void stop() {
        org.opennms.netmgt.scriptd.Scriptd scriptd = org.opennms.netmgt.scriptd.Scriptd.getInstance();
        scriptd.stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        org.opennms.netmgt.scriptd.Scriptd scriptd = org.opennms.netmgt.scriptd.Scriptd.getInstance();
        return scriptd.getStatus();
    }

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String status() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getStatusText() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    @Override
    public long getStartTimeMilliseconds() {
        throw new NotImplementedException();
    }
}
