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
package org.opennms.netmgt.vacuumd.jmx;

import org.apache.commons.lang.NotImplementedException;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;

/**
 * Implementws the VacuumdMBead interface and delegeates the mbean
 * implementation to the single Vacuumd.
 *
 * @author ranger
 * @version $Id: $
 */
public class Vacuumd implements VacuumdMBean {

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.vacuumd.jmx.VacuumdMBean#init()
     */
    /**
     * <p>init</p>
     */
    @Override
    public void init() {

        EventIpcManagerFactory.init();
        EventIpcManager mgr = EventIpcManagerFactory.getIpcManager();
        getVacuumd().setEventManager(mgr);
        getVacuumd().init();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.vacuumd.jmx.VacuumdMBean#start()
     */
    /**
     * <p>start</p>
     */
    @Override
    public void start() {
        getVacuumd().start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.vacuumd.jmx.VacuumdMBean#stop()
     */
    /**
     * <p>stop</p>
     */
    @Override
    public void stop() {
        getVacuumd().stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.vacuumd.jmx.VacuumdMBean#getStatus()
     */
    /**
     * <p>getStatus</p>
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return getVacuumd().getStatus();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.vacuumd.jmx.VacuumdMBean#status()
     */
    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String status() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.vacuumd.jmx.VacuumdMBean#getStatusText()
     */
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

    /** {@inheritDoc} */
    @Override
    public long getNumAutomations() {
        return getVacuumd().getNumAutomations();
    }

    private org.opennms.netmgt.vacuumd.Vacuumd getVacuumd() {
        return org.opennms.netmgt.vacuumd.Vacuumd.getSingleton();
    }

}
