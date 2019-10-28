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

package org.opennms.netmgt.config.dao.outages.impl;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;

import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.config.BasicScheduleUtils;
import org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao;
import org.opennms.netmgt.config.poller.outages.Interface;
import org.opennms.netmgt.config.poller.outages.Node;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Time;

public abstract class AbstractPollOutagesDao implements ReadablePollOutagesDao {
    public static final String JSON_STORE_KEY = "poll-outages";

    final JsonStore jsonStore;

    public AbstractPollOutagesDao() {
        jsonStore = null;
        reload();
    }

    AbstractPollOutagesDao(JsonStore jsonStore) {
        this.jsonStore = Objects.requireNonNull(jsonStore);
    }

    @Override
    public boolean isNodeIdInOutage(long lnodeid, String outName) {
        final Outage out = getReadOnlyConfig().getOutage(outName);
        if (out == null) return false;
        return isNodeIdInOutage(lnodeid, out);
    }

    @Override
    public boolean isInterfaceInOutage(String linterface, String outName) {
        final Outage out = getReadOnlyConfig().getOutage(outName);
        if (out == null) return false;
        return isInterfaceInOutage(linterface, out);
    }

    @Override
    public boolean isCurTimeInOutage(String outName) {
        return isTimeInOutage(new GregorianCalendar(), outName);
    }

    @Override
    public boolean isTimeInOutage(long time, String outName) {
        final Outage out = getReadOnlyConfig().getOutage(outName);
        if (out == null) return false;

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return isTimeInOutage(cal, out);
    }

    @Override
    public String getOutageType(String name) {
        final Outage out = getReadOnlyConfig().getOutage(name);
        if (out == null) return null;
        return out.getType();
    }

    @Override
    public List<Time> getOutageTimes(String name) {
        final Outage out = getReadOnlyConfig().getOutage(name);
        if (out == null) return null;
        return out.getTimes();
    }

    @Override
    public List<Interface> getInterfaces(String name) {
        final Outage out = getReadOnlyConfig().getOutage(name);
        if (out == null) return null;
        return out.getInterfaces();
    }

    @Override
    public boolean isTimeInOutage(Calendar cal, String outName) {
        final Outage out = getReadOnlyConfig().getOutage(outName);
        if (out == null) return false;

        return isTimeInOutage(cal, out);
    }

    @Override
    public List<Node> getNodeIds(String name) {
        final Outage out = getReadOnlyConfig().getOutage(name);
        if (out == null) return null;
        return out.getNodes();
    }

    @Override
    public Calendar getEndOfOutage(String outName) {
        final Outage out = getReadOnlyConfig().getOutage(outName);
        if (out == null) return null;
        return getEndOfOutage(out);
    }

    @Override
    public boolean isInterfaceInOutage(final String linterface, final Outage out) {
        if (out == null) return false;

        for (final Interface ointerface : out.getInterfaces()) {
            if (ointerface.getAddress().equals("match-any") || ointerface.getAddress().equals(linterface)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isTimeInOutage(final Calendar cal, final Outage outage) {
        return BasicScheduleUtils.isTimeInSchedule(cal, BasicScheduleUtils.getBasicOutageSchedule(outage));
    }

    @Override
    public boolean isCurTimeInOutage(final Outage out) {
        return isTimeInOutage(new GregorianCalendar(), out);
    }

    @Override
    public Calendar getEndOfOutage(final Outage out) {
        // FIXME: We need one that takes the time as a parm.  This makes it more testable
        return BasicScheduleUtils.getEndOfSchedule(BasicScheduleUtils.getBasicOutageSchedule(out));
    }

    @Override
    public boolean isNodeIdInOutage(final long lnodeid, final Outage out) {
        if (out == null) return false;

        for (final Node onode : out.getNodes()) {
            if (onode.getId() == lnodeid) {
                return true;
            }
        }

        return false;
    }
}
