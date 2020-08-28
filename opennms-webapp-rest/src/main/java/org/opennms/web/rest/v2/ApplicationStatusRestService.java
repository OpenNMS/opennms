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
import org.opennms.netmgt.collection.support.builder.PerspectiveResponseTimeResource;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.perspectivepolling.ApplicationServiceStatus;
import org.opennms.netmgt.model.perspectivepolling.ApplicationStatus;
import org.opennms.netmgt.model.perspectivepolling.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.MoreObjects;

@Component
@Path("perspectivepoller")
@Transactional
public class ApplicationStatusRestService {

    @Autowired
    private OutageDao outageDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private MonitoredServiceDao monitoredServiceDao;

    class DowntimeInterval {
        public long start, end;

        public DowntimeInterval(final OnmsOutage onmsOutage, long start, long end) {
            this.start = onmsOutage.getIfLostService().getTime();

            if (this.start < start) {
                this.start = start;
            }

            this.end = onmsOutage.getIfRegainedService() != null ? onmsOutage.getIfRegainedService().getTime() : end;

            if (this.end > end) {
                this.end = end;
            }
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

    private boolean intervalsOverlap(final DowntimeInterval downtimeInterval1, final DowntimeInterval downtimeInterval2) {
        return !(Math.max(downtimeInterval1.start, downtimeInterval2.start) > Math.min(downtimeInterval1.end, downtimeInterval2.end));
    }

    public double calculateApplicationPercentageUptime(final List<DowntimeInterval> intervals, final long start, final long end) {
        double totalTimeMillis = end - start;
        double uptimeMillis = totalTimeMillis;
        for(final DowntimeInterval interval : intervals) {
            uptimeMillis -= (Math.min(interval.end, end)-Math.max(interval.start, start));
        }
        return uptimeMillis / totalTimeMillis;
    }

    private ApplicationStatus buildApplicationStatus(final OnmsApplication onmsApplication, final Collection<OnmsOutage> onmsOutages, final long start, final long end) {
        final Map<OnmsMonitoringLocation, List<DowntimeInterval>> m = new HashMap<>();

        for (final OnmsMonitoringLocation onmsMonitoringLocation : onmsApplication.getPerspectiveLocations()) {
            m.put(onmsMonitoringLocation, new ArrayList<>());
        }

        for(final OnmsOutage onmsOutage : onmsOutages) {
            m.get(onmsOutage.getPerspective()).add(new DowntimeInterval(onmsOutage, start, end));
        }

        final ApplicationStatus applicationStatus = new ApplicationStatus();
        applicationStatus.setStart(start);
        applicationStatus.setEnd(end);
        applicationStatus.setApplicationId(onmsApplication.getId());

        for (final OnmsMonitoringLocation onmsMonitoringLocation : onmsApplication.getPerspectiveLocations()) {
            final Location location = new Location();
            location.setName(onmsMonitoringLocation.getLocationName());

            final List<DowntimeInterval> mergedDowntimeIntervals = mergeDowntimeIntervals(m.get(onmsMonitoringLocation));

            location.setAggregatedStatus(100.0 * calculateApplicationPercentageUptime(mergedDowntimeIntervals, start, end));
            applicationStatus.getLocations().add(location);
        }
        return applicationStatus;
    }

    private ApplicationServiceStatus buildApplicationServiceStatus(final OnmsApplication onmsApplication, final Integer monitoredServiceId, final Collection<OnmsOutage> onmsOutages, final long start, final long end) {
        final OnmsMonitoredService onmsMonitoredService = monitoredServiceDao.get(monitoredServiceId);

        final Map<OnmsMonitoringLocation, List<DowntimeInterval>> m = new HashMap<>();

        for (final OnmsMonitoringLocation onmsMonitoringLocation : onmsApplication.getPerspectiveLocations()) {
            m.put(onmsMonitoringLocation, new ArrayList<>());
        }

        for(final OnmsOutage onmsOutage : onmsOutages) {
            if (monitoredServiceId.equals(onmsOutage.getMonitoredService().getId())) {
                m.get(onmsOutage.getPerspective()).add(new DowntimeInterval(onmsOutage, start, end));
            }
        }

        final ApplicationServiceStatus applicationServiceStatus = new ApplicationServiceStatus();
        applicationServiceStatus.setStart(start);
        applicationServiceStatus.setEnd(end);
        applicationServiceStatus.setApplicationId(onmsApplication.getId());
        applicationServiceStatus.setMonitoredServiceId(monitoredServiceId);

        for (final OnmsMonitoringLocation onmsMonitoringLocation : onmsApplication.getPerspectiveLocations()) {
            final Location location = new Location();
            location.setName(onmsMonitoringLocation.getLocationName());

            final List<DowntimeInterval> mergedDowntimeIntervals = mergeDowntimeIntervals(m.get(onmsMonitoringLocation));

            location.setAggregatedStatus(100.0 * calculateApplicationPercentageUptime(mergedDowntimeIntervals, start, end));
            final PerspectiveResponseTimeResource perspectiveResponseTimeResource = new PerspectiveResponseTimeResource(location.getName(), InetAddressUtils.toIpAddrString(onmsMonitoredService.getIpAddress()), onmsMonitoredService.getServiceType().getName());
            location.setResponseResourceId(perspectiveResponseTimeResource.getInstance());
            applicationServiceStatus.getLocations().add(location);
        }

        return applicationServiceStatus;
    }

    @GET
    @Path("{applicationId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response applicationStatus(@PathParam("applicationId") final Integer applicationId, @QueryParam("start") Long start, @QueryParam("end") Long end) {
        if (end == null) {
            end = new Date().getTime();
        }

        if (start == null) {
            start = end - 86400000;
        }

        final OnmsApplication onmsApplication = applicationDao.get(applicationId);
        if (onmsApplication == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Collection<OnmsOutage> statusChanges = outageDao.getStatusChangesForApplicationIdBetween(new Date(start), new Date(end), applicationId);
        return Response.ok(buildApplicationStatus(onmsApplication, statusChanges, start, end)).build();
    }

    @GET
    @Path("{applicationId}/{monitoredServiceId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response applicationServiceStatus(@PathParam("applicationId") final Integer applicationId, @PathParam("monitoredServiceId") final Integer monitoredServiceId, @QueryParam("start") Long start, @QueryParam("end") Long end) {
        if (end == null) {
            end = new Date().getTime();
        }

        if (start == null) {
            start = end - 86400000;
        }

        final OnmsApplication onmsApplication = applicationDao.get(applicationId);
        if (onmsApplication == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Collection<OnmsOutage> statusChanges = outageDao.getStatusChangesForApplicationIdBetween(new Date(start), new Date(end), applicationId);
        return Response.ok(buildApplicationServiceStatus(onmsApplication, monitoredServiceId, statusChanges, start, end)).build();
    }
}
