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
