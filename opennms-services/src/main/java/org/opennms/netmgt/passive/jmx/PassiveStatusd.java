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
package org.opennms.netmgt.passive.jmx;

import org.apache.commons.lang.NotImplementedException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.passive.PassiveStatusKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>PassiveStatusd class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class PassiveStatusd extends AbstractServiceDaemon implements PassiveStatusdMBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(PassiveStatusd.class);
    /**
     * <p>Constructor for PassiveStatusd.</p>
     */
    public PassiveStatusd() {
        super(NAME);
    }

    public final static String NAME = "passive";

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        EventIpcManagerFactory.init();
        EventIpcManager mgr = EventIpcManagerFactory.getIpcManager();

        PassiveStatusKeeper keeper = getPassiveStatusKeeper();
        keeper.setEventManager(mgr);
        keeper.setDataSource(DataSourceFactory.getInstance());
        keeper.init();
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        getPassiveStatusKeeper().start();
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        getPassiveStatusKeeper().stop();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return getPassiveStatusKeeper().getStatus();
    }

    @Override
    public long getStartTimeMilliseconds() {
        throw new NotImplementedException();
    }

    private PassiveStatusKeeper getPassiveStatusKeeper() {
        return PassiveStatusKeeper.getInstance();
    }
}
