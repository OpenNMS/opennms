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
package org.opennms.netmgt.config.dao.outages.api;

import java.util.Calendar;
import java.util.List;

import org.opennms.netmgt.config.dao.common.api.ReadableDao;
import org.opennms.netmgt.config.poller.outages.Interface;
import org.opennms.netmgt.config.poller.outages.Node;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Outages;
import org.opennms.netmgt.config.poller.outages.Time;

public interface ReadablePollOutagesDao extends ReadableDao<Outages> {
    /**
     * Return if the node represented by the nodeid is part of specified outage.
     *
     * @param lnodeid
     *            the nodeid to be checked
     * @param outName
     *            the outage name
     * @return the node is part of the specified outage
     */
    boolean isNodeIdInOutage(long lnodeid, String outName);

    /**
     * Return if interfaces is part of specified outage.
     *
     * @param linterface
     *            the interface to be looked up
     * @param outName
     *            the outage name
     * @return the interface is part of the specified outage
     */
    boolean isInterfaceInOutage(String linterface, String outName);

    /**
     * Return if current time is part of specified outage.
     *
     * @param outName
     *            the outage name
     * @return true if current time is in outage
     */
    boolean isCurTimeInOutage(String outName);

    /**
     * Return if time is part of specified outage.
     *
     * @param time
     *            the time in millis to look up
     * @param outName
     *            the outage name
     * @return true if time is in outage
     */
    boolean isTimeInOutage(long time, String outName);

    /**
     * Return the type for specified outage.
     *
     * @param name
     *            the outage that is to be looked up
     * @return the type for the specified outage, null if not found
     */
    String getOutageType(final String name);

    /**
     * Return the outage times for specified outage.
     *
     * @param name
     *            the outage that is to be looked up
     * @return the outage times for the specified outage, null if not found
     */
    List<Time> getOutageTimes(final String name);

    /**
     * Return the interfaces for specified outage.
     *
     * @param name
     *            the outage that is to be looked up
     * @return the interfaces for the specified outage, null if not found
     */
    List<Interface> getInterfaces(final String name);

    /**
     * Return if interfaces is part of specified outage.
     *
     * @param linterface
     *            the interface to be looked up
     * @param getOutageSchedule(out)
     *            the outage
     * @return the interface is part of the specified outage
     */
    boolean isInterfaceInOutage(final String linterface, final Outage out);

    /**
     * {@inheritDoc}
     *
     * Return if time is part of specified outage.
     */
    boolean isTimeInOutage(final Calendar cal, final String outName);

    /**
     * Return if time is part of specified outage.
     *
     * @param cal
     *            the calendar to lookup
     * @param getOutageSchedule(outage)
     *            the outage
     * @return true if time is in outage
     */
    boolean isTimeInOutage(final Calendar cal, final Outage outage);

    /**
     * Return if current time is part of specified outage.
     *
     * @param getOutageSchedule(out)
     *            the outage
     * @return true if current time is in outage
     */
    boolean isCurTimeInOutage(final Outage out);

    /**
     * <p>getNodeIds</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return an array of {@link org.opennms.netmgt.config.poller.outages.Node} objects.
     */
    List<Node> getNodeIds(final String name);

    /**
     * <p>getEndOfOutage</p>
     *
     * @param outName a {@link java.lang.String} object.
     * @return a {@link java.util.Calendar} object.
     */
    Calendar getEndOfOutage(final String outName);

    /**
     * Return a calendar representing the end time of this outage, assuming it's
     * currently active (i.e. right now is within one of the time periods)
     *
     * FIXME: This code is almost identical to isTimeInOutage... We need to fix
     * it
     *
     * @param getOutageSchedule(out) a {@link org.opennms.netmgt.config.poller.outages.Outage} object.
     * @return a {@link java.util.Calendar} object.
     */
    Calendar getEndOfOutage(final Outage out);

    /**
     * <p>
     * Return if nodeid is part of specified outage
     * </p>
     *
     * @param lnodeid
     *            the nodeid to be looked up
     * @return the node iis part of the specified outage
     * @param getOutageSchedule(out) a {@link org.opennms.netmgt.config.poller.outages.Outage} object.
     */
    boolean isNodeIdInOutage(final long lnodeid, final Outage out);
}
