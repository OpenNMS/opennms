/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.support.builder.RemoteLatencyResource;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.LocationSpecificStatusDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.remotepolling.ApplicationServiceStatus;
import org.opennms.netmgt.model.remotepolling.ApplicationStatus;
import org.opennms.netmgt.model.remotepolling.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;

@Component
@Path("remotepoller")
@Transactional
public class ApplicationStatusRestService {

    @Autowired
    private LocationSpecificStatusDao locationSpecificStatusDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    class DowntimeInterval {
        public long start, end;

        public DowntimeInterval(final long start, final long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("start", start)
                    .add("end", end)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DowntimeInterval that = (DowntimeInterval) o;
            return start == that.start &&
                    end == that.end;
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }
    }

    private List<DowntimeInterval> mergeDowntimeIntervals(final List<DowntimeInterval> intervals) {
        final List<DowntimeInterval> resultList = new ArrayList<>();

        Collections.sort(intervals, (DowntimeInterval donwtimeInterval1, DowntimeInterval donwtimeInterval2) -> {
            return Long.signum(donwtimeInterval1.start - donwtimeInterval2.start) != 0 ? Long.signum(donwtimeInterval1.start - donwtimeInterval2.start) : Long.signum(donwtimeInterval1.end - donwtimeInterval2.end);
        });

        for (int i = 0; i < intervals.size(); ++i) {
            final DowntimeInterval mergedInterval = intervals.get(i);

            ++i;
            while (i < intervals.size() && intervalsOverlap(mergedInterval, intervals.get(i))) {
                mergedInterval.start = Math.min(mergedInterval.start, intervals.get(i).start);
                mergedInterval.end = Math.max(mergedInterval.end, intervals.get(i).end);
                ++i;
            }
            --i;

            resultList.add(mergedInterval);
        }

        return resultList;
    }

    private boolean intervalsOverlap(final DowntimeInterval donwtimeInterval1, final DowntimeInterval donwtimeInterval2) {
        return !(Math.max(donwtimeInterval1.start, donwtimeInterval2.start) > Math.min(donwtimeInterval1.end, donwtimeInterval2.end));
    }

    private double calculateServicePercentageUptime(final Collection<OnmsLocationSpecificStatus> statusChanges, final List<DowntimeInterval> downtimeIntervals, final long start, final long end) {
        double totalTimeMillis = end - start;
        double uptimeMillis = totalTimeMillis;

        OnmsLocationSpecificStatus lastChange = null;

        for(final OnmsLocationSpecificStatus status : statusChanges) {
            long s = (lastChange == null ? 0 : lastChange.getPollResult().getTimestamp().getTime());
            long e = status.getPollResult().getTimestamp().getTime();
            if (lastChange != null && lastChange.getPollResult().isDown() ) {
                if(s < e) {
                    uptimeMillis -= (Math.min(e, end)-Math.max(s, start));
                    downtimeIntervals.add(new DowntimeInterval(Math.max(s, start), Math.min(e, end)));
                }
            }
            lastChange = status;
        }

        if (lastChange != null && lastChange.getPollResult().isDown() ) {
            long s = lastChange.getPollResult().getTimestamp().getTime();
            long e = end;
            if(s < e) {
                uptimeMillis -= (Math.min(e, end)-Math.max(s, start));
                downtimeIntervals.add(new DowntimeInterval(Math.max(s, start), Math.min(e, end)));
            }
        }

        return uptimeMillis / totalTimeMillis;
    }

    public double calculateApplicationPercentageUptime(final List<DowntimeInterval> intervals, final long start, final long end) {
        double totalTimeMillis = end - start;
        double uptimeMillis = totalTimeMillis;
        for(final DowntimeInterval interval : intervals) {
            uptimeMillis -= (Math.min(interval.end, end)-Math.max(interval.start, start));
        }
        return uptimeMillis / totalTimeMillis;
    }


    private ApplicationStatus getApplicationStatus(final OnmsApplication onmsApplication, final Collection<OnmsLocationSpecificStatus> statusChanges, final long start, final long end) {
        final Map<OnmsMonitoringLocation, Map<OnmsMonitoredService, List<OnmsLocationSpecificStatus>>> m = new HashMap<>();

        for(final OnmsLocationSpecificStatus onmsLocationSpecificStatus : statusChanges) {
            m.computeIfAbsent(onmsLocationSpecificStatus.getLocation(), k -> new HashMap<>())
                    .computeIfAbsent(onmsLocationSpecificStatus.getMonitoredService(), k -> new ArrayList<>())
                    .add(onmsLocationSpecificStatus);
        }

        final ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStart(start);
        applicationStatus.setEnd(end);
        applicationStatus.setApplicationId(onmsApplication.getId());

        for (final OnmsMonitoringLocation onmsMonitoringLocation : m.keySet()) {
            final Location location = new Location();
            location.setName(onmsMonitoringLocation.getLocationName());

            final List<DowntimeInterval> downtimeIntervals = Lists.newArrayList();
            
            for (final OnmsMonitoredService onmsMonitoredService : m.get(onmsMonitoringLocation).keySet()) {
                calculateServicePercentageUptime(m.get(onmsMonitoringLocation).get(onmsMonitoredService), downtimeIntervals, start, end);
            }

            final List<DowntimeInterval> mergedDowntimeIntervals = mergeDowntimeIntervals(downtimeIntervals);

            location.setAggregatedStatus(100.0 * calculateApplicationPercentageUptime(mergedDowntimeIntervals, start, end));
            applicationStatus.getLocations().add(location);
        }
        return applicationStatus;
    }

    private ApplicationServiceStatus getApplicationServiceStatus(final OnmsApplication onmsApplication, final Integer monitoredServiceId, final Collection<OnmsLocationSpecificStatus> statusChanges, final long start, final long end) {
        final Map<OnmsMonitoringLocation, List<OnmsLocationSpecificStatus>> m = new HashMap<>();
        final OnmsMonitoredService onmsMonitoredService = monitoredServiceDao.get(monitoredServiceId);

        for(final OnmsLocationSpecificStatus onmsLocationSpecificStatus : statusChanges) {
            if (onmsLocationSpecificStatus.getMonitoredService().getId().equals(monitoredServiceId)) {
                m.computeIfAbsent(onmsLocationSpecificStatus.getLocation(), k -> new ArrayList<>())
                        .add(onmsLocationSpecificStatus);
            }
        }

        final ApplicationServiceStatus applicationServiceStatus = new ApplicationServiceStatus();
        applicationServiceStatus.setStart(start);
        applicationServiceStatus.setEnd(end);
        applicationServiceStatus.setApplicationId(onmsApplication.getId());
        applicationServiceStatus.setMonitoredServiceId(monitoredServiceId);

        for (final OnmsMonitoringLocation onmsMonitoringLocation : m.keySet()) {
            final Location location = new Location();
            location.setName(onmsMonitoringLocation.getLocationName());
            location.setAggregatedStatus(100.0 * calculateServicePercentageUptime(m.get(onmsMonitoringLocation), Lists.newArrayList(), start, end));
            RemoteLatencyResource remoteLatencyResource = new RemoteLatencyResource(location.getName(), InetAddressUtils.toIpAddrString(onmsMonitoredService.getIpAddress()), onmsMonitoredService.getServiceType().getName());
            location.setResponseResourceId(remoteLatencyResource.getInstance());
            applicationServiceStatus.getLocations().add(location);
        }
        return applicationServiceStatus;
    }

    @GET
    @Path("{applicationId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response applicationStatus(@PathParam("applicationId") final Integer applicationId, @QueryParam("start") final Long start, @QueryParam("end") final Long end) {
        final OnmsApplication onmsApplication = applicationDao.get(applicationId);
        if (onmsApplication == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Collection<OnmsLocationSpecificStatus> statusChanges = locationSpecificStatusDao.getStatusChangesForApplicationIdBetween(new Date(start), new Date(end), applicationId);
        return Response.ok(getApplicationStatus(onmsApplication, statusChanges, start, end)).build();
    }

    @GET
    @Path("{applicationId}/{monitoredServiceId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response applicationServiceStatus(@PathParam("applicationId") final Integer applicationId, @PathParam("monitoredServiceId") final Integer monitoredServiceId, @QueryParam("start") final Long start, @QueryParam("end") final Long end) {
        final OnmsApplication onmsApplication = applicationDao.get(applicationId);
        if (onmsApplication == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Collection<OnmsLocationSpecificStatus> statusChanges = locationSpecificStatusDao.getStatusChangesForApplicationIdBetween(new Date(start), new Date(end), applicationId);
        return Response.ok(getApplicationServiceStatus(onmsApplication, monitoredServiceId, statusChanges, start, end)).build();
    }
}
