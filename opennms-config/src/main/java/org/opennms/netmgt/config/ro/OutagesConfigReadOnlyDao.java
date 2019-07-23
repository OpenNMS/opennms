/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.ro;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.BasicScheduleUtils;
import org.opennms.netmgt.config.api.PollOutagesConfig;
import org.opennms.netmgt.config.poller.outages.Interface;
import org.opennms.netmgt.config.poller.outages.Node;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Outages;
import org.opennms.netmgt.config.poller.outages.Time;

public class OutagesConfigReadOnlyDao extends AbstractReadOnlyConfigDao<Outages> implements PollOutagesConfig {

    private final String fileName = ConfigFileConstants.getFileName(ConfigFileConstants.POLL_OUTAGES_CONFIG_FILE_NAME);

    private final long cacheLengthInMillis = SystemProperties.getLong("org.opennms.netmgt.config.ro.OutagesConfig.cacheTtlMillis", DEFAULT_CACHE_MILLIS);

    @Override
    public Outages getConfig() {
        return getByKey(Outages.class, fileName, cacheLengthInMillis);
    }

    @Override
    public List<Interface> getInterfaces(String name) {
        final Outage outage = getOutage(name);
        return outage == null ? null : outage.getInterfaces();
    }

    @Override
    public List<Node> getNodeIds(String name) {
        final Outage outage = getOutage(name);
        return outage == null ? null : outage.getNodes();
    }

    @Override
    public Outage getOutage(String name) {
        Outages outages = getConfig();
        return outages == null ? null : outages.getOutage(name);
    }

    @Override
    public List<Outage> getOutages() {
        Outages outages = getConfig();
        return outages == null ? null : outages.getOutages();
    }

    @Override
    public List<Time> getOutageTimes(String name) {
        final Outage outage = getOutage(name);
        return outage == null ? null : outage.getTimes();
    }

    @Override
    public String getOutageType(String name) {
        final Outage outage = getOutage(name);
        return outage == null ? null : outage.getType();
    }

    // - FIXME move to modifiable interface only
    @Override
    public Lock getReadLock() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isCurTimeInOutage(Outage outage) {
        return isTimeInOutage(new GregorianCalendar(), outage);
    }

    @Override
    public boolean isCurTimeInOutage(String outageName) {
        return isTimeInOutage(new GregorianCalendar(), outageName);
    }

    @Override
    public boolean isInterfaceInOutage(String linterface, Outage outage) {
        if (outage == null) {
            return false;
        }
        for (final Interface ointerface : outage.getInterfaces()) {
            if (ointerface.getAddress().equals("match-any") || ointerface.getAddress().equals(linterface)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isInterfaceInOutage(String linterface, String outageName) {
        return isInterfaceInOutage(linterface, getOutage(outageName));
    }

    @Override
    public boolean isNodeIdInOutage(long lnodeid, Outage outage) {
        if (outage == null) {
            return false;
        }
        for (final Node onode : outage.getNodes()) {
            if (onode.getId() == lnodeid) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNodeIdInOutage(long lnodeid, String outageName) {
        return isNodeIdInOutage(lnodeid, getOutageType(outageName));
    }

    @Override
    public boolean isTimeInOutage(long time, String outageName) {
        final Outage outage = getOutage(outageName);
        if (outage == null)
            return false;

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return isTimeInOutage(calendar, outage);
    }

    @Override
    public void update() {
        // force expiry of the cache and reload
        cachedAt = 0;
        getConfig();
    }

    private boolean isTimeInOutage(GregorianCalendar gregorianCalendar, String outageName) {
        final Outage outage = getOutage(outageName);
        return outage == null ? false : isTimeInOutage(gregorianCalendar, outage);
    }

    private boolean isTimeInOutage(GregorianCalendar gregorianCalendar, Outage outage) {
        return BasicScheduleUtils.isTimeInSchedule(gregorianCalendar, BasicScheduleUtils.getBasicOutageSchedule(outage));
    }

    private boolean isTimeInOutage(Calendar calendar, Outage outage) {
        return BasicScheduleUtils.isTimeInSchedule(calendar, BasicScheduleUtils.getBasicOutageSchedule(outage));
    }

    @Override
    public Calendar getEndOfOutage(String scheduledOutageName) {
        final Outage outage = getOutage(scheduledOutageName);
        if (outage == null)
            return null;
        return getEndOfOutage(outage);
    }

    private Calendar getEndOfOutage(final Outage outage) {
        return BasicScheduleUtils.getEndOfSchedule(BasicScheduleUtils.getBasicOutageSchedule(outage));
    }

}
