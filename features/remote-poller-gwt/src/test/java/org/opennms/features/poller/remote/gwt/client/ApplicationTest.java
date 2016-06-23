/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

public class ApplicationTest {
    final Date m_to = new Date();

    final Date m_from = new Date(m_to.getTime() - (1000 * 60 * 60 * 24));

    final static long FIVE_MINUTES = 1000 * 60 * 5;

    static int count = 0;

    static int monitorOffset = 0;

    final static Random m_random = new Random();

    Map<String, ApplicationInfo> m_applications = null;

    Map<String, GWTMonitoredService> m_services = null;

    Map<String, GWTLocationMonitor> m_monitors = null;

    @Before
    public void setup() {
        m_applications = new HashMap<String, ApplicationInfo>();
        m_services = new HashMap<String, GWTMonitoredService>();
        m_monitors = new HashMap<String, GWTLocationMonitor>();
        count = 0;
        monitorOffset = 0;
    }

    @Test
    public void testApplicationStatusUnknown() {
        ApplicationDetails appStatus = new ApplicationDetails(getApplication("TestApp1"), m_from, m_to, null, null);
        assertEquals(Status.UNKNOWN, appStatus.getStatusDetails().getStatus());
    }

    @Test
    public void testApplicationStatusUp() {
        ApplicationDetails status = getUpApplicationStatus("TestApp1");

        assertEquals(Status.UP, status.getStatusDetails().getStatus());
        assertEquals(Double.valueOf(100.0), status.getAvailability());
    }

    @Test
    public void testApplicationStatusMarginal() {
        ApplicationDetails status = getMarginalApplicationStatus("TestApp1");

        assertEquals(Status.MARGINAL, status.getStatusDetails().getStatus());
    }

    @Test
    public void testApplicationStatusDown() {
        ApplicationDetails status = getDownApplicationStatus("TestApp1");

        assertEquals(Status.DOWN, status.getStatusDetails().getStatus());
    }

    @Test
    public void testApplicationStateUnknown() {
        Collection<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
        applications.add(getApplication("TestApp1"));
        applications.add(getApplication("TestApp2"));

        Map<String, List<GWTLocationSpecificStatus>> appStatuses = new HashMap<String, List<GWTLocationSpecificStatus>>();
        appStatuses.put("TestApp1", getUpStatusList());
        appStatuses.put("TestApp1", getUpStatusList());
        ApplicationState appState = new ApplicationState(m_from, m_to, applications, getMonitors(appStatuses.values()), appStatuses);

        assertEquals(Status.UNKNOWN, appState.getStatusDetails().getStatus());
    }

    @Test
    public void testApplicationStateMarginal() {
        Collection<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
        applications.add(getApplication("TestApp1"));
        applications.add(getApplication("TestApp2"));

        Map<String, List<GWTLocationSpecificStatus>> appStatuses = new HashMap<String, List<GWTLocationSpecificStatus>>();
        appStatuses.put("TestApp1", getMarginalStatusList());
        appStatuses.put("TestApp2", getUpStatusList());
        ApplicationState appState = new ApplicationState(m_from, m_to, applications, getMonitors(appStatuses.values()), appStatuses);

        assertEquals(Status.MARGINAL, appState.getStatusDetails().getStatus());
    }

    @Test
    public void testApplicationStateDown() {
        Collection<ApplicationInfo> applications = new ArrayList<ApplicationInfo>();
        applications.add(getApplication("TestApp1"));
        applications.add(getApplication("TestApp2"));

        Map<String, List<GWTLocationSpecificStatus>> appStatuses = new HashMap<String, List<GWTLocationSpecificStatus>>();
        appStatuses.put("TestApp1", getDownStatusList());
        appStatuses.put("TestApp2", getUpStatusList());
        ApplicationState appState = new ApplicationState(m_from, m_to, applications, getMonitors(appStatuses.values()), appStatuses);

        assertEquals(Status.DOWN, appState.getStatusDetails().getStatus());
    }

    @Test
    public void testSet() {
        final Set<ApplicationInfo> applicationSet = new HashSet<ApplicationInfo>();

        final ApplicationInfo ai1u = new ApplicationInfo(1, "test1", new TreeSet<GWTMonitoredService>(), new TreeSet<String>(), StatusDetails.unknown());
        final ApplicationInfo ai2u = new ApplicationInfo(2, "test2", new TreeSet<GWTMonitoredService>(), new TreeSet<String>(), StatusDetails.unknown());
        final ApplicationInfo ai1d = new ApplicationInfo(1, "test1", new TreeSet<GWTMonitoredService>(), new TreeSet<String>(), StatusDetails.down("busted"));
        final ApplicationInfo ai2d = new ApplicationInfo(2, "test2", new TreeSet<GWTMonitoredService>(), new TreeSet<String>(), StatusDetails.down("busted"));
        applicationSet.add(ai1u);
        applicationSet.add(ai2u);
        applicationSet.add(ai1d);
        applicationSet.add(ai2d);

        assertEquals("applications should be equal", ai1u, ai1d);
        assertEquals("hashcodes should be equal", ai1u.hashCode(), ai1d.hashCode());
        assertEquals(2, applicationSet.size());
    }

    private ApplicationDetails getDownApplicationStatus(final String appName) {
        List<GWTLocationSpecificStatus> statuses = getDownStatusList();
        ApplicationDetails status = new ApplicationDetails(getApplication(appName), m_from, m_to, getMonitors(statuses), statuses);
        return status;
    }

    private List<GWTLocationMonitor> getMonitors(final Collection<List<GWTLocationSpecificStatus>> statuseses) {
        final List<GWTLocationMonitor> monitors = new ArrayList<GWTLocationMonitor>();
        for (final List<GWTLocationSpecificStatus> statuses : statuseses) {
            for (final GWTLocationSpecificStatus status : statuses) {
                if (!monitors.contains(status.getLocationMonitor())) {
                    monitors.add(status.getLocationMonitor());
                }
            }
        }
        return monitors;
    }

    private List<GWTLocationMonitor> getMonitors(final List<GWTLocationSpecificStatus> statuses) {
        return getMonitors(Collections.singletonList(statuses));
    }

    private List<GWTLocationSpecificStatus> getDownStatusList() {
        List<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();

        int offset = monitorOffset;

        // identical overlaps
        Date date = m_from;
        statuses.add(getUpStatus(date, "RDU", Integer.toString(offset + 1)));
        statuses.add(getUpStatus(date, "RDU", Integer.toString(offset + 2)));
        statuses.add(getUpStatus(date, "RDU", Integer.toString(offset + 3)));

        date = new Date(date.getTime() + 1000);
        statuses.add(getDownStatus(date, "RDU", Integer.toString(offset + 1)));
        date = new Date(date.getTime() + 1000);
        statuses.add(getDownStatus(date, "RDU", Integer.toString(offset + 2)));
        date = new Date(date.getTime() + 1000);
        statuses.add(getDownStatus(date, "RDU", Integer.toString(offset + 3)));

        date = new Date(date.getTime() + FIVE_MINUTES);
        statuses.add(getDownStatus(date, "RDU", Integer.toString(offset + 1)));
        date = new Date(date.getTime() + 1000);
        statuses.add(getDownStatus(date, "RDU", Integer.toString(offset + 2)));
        date = new Date(date.getTime() + 1000);
        statuses.add(getDownStatus(date, "RDU", Integer.toString(offset + 3)));

        monitorOffset += offset;
        return statuses;
    }

    private ApplicationDetails getUpApplicationStatus(final String appName) {
        List<GWTLocationSpecificStatus> statuses = getUpStatusList();
        ApplicationDetails status = new ApplicationDetails(getApplication(appName), m_from, m_to, getMonitors(statuses), statuses);
        return status;
    }

    private List<GWTLocationSpecificStatus> getUpStatusList() {
        List<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();
        Date date = m_from;
        for (int i = 0; i < 5; i++) {
            statuses.add(getStatus(date, "RDU", Integer.toString(monitorOffset + 1), up(date)));
            date = new Date(date.getTime() + FIVE_MINUTES);
        }
        monitorOffset += 1;
        return statuses;
    }

    private ApplicationDetails getMarginalApplicationStatus(final String appName) {
        List<GWTLocationSpecificStatus> statuses = getMarginalStatusList();
        ApplicationDetails status = new ApplicationDetails(getApplication(appName), m_from, m_to, getMonitors(statuses), statuses);
        return status;
    }

    private List<GWTLocationSpecificStatus> getMarginalStatusList() {
        List<GWTLocationSpecificStatus> statuses = new ArrayList<GWTLocationSpecificStatus>();
        Date date = m_from;
        for (int i = 0; i < 6; i++) {
            statuses.add(getStatus(date, "RDU", Integer.toString(monitorOffset + 1), up(date)));
            statuses.add(getStatus(date, "RDU", Integer.toString(monitorOffset + 2), down(date, "I'm so high, I have no idea what's going on!")));
            date = new Date(date.getTime() + FIVE_MINUTES);
        }
        statuses.add(getStatus(date, "RDU", Integer.toString(monitorOffset + 1), up(date)));
        statuses.add(getStatus(date, "RDU", Integer.toString(monitorOffset + 2), down(date, "Still totally broken.")));

        monitorOffset += 2;
        return statuses;
    }

    private GWTLocationSpecificStatus getUpStatus(Date date, String monitorName, String monitorId) {
        return getStatus(date, monitorName, monitorId, up(date));
    }

    private GWTLocationSpecificStatus getDownStatus(Date date, String monitorName, String monitorId) {
        return getStatus(date, monitorName, monitorId, down(date, "Stuff be broke, yo!"));
    }

    private GWTPollResult up(Date date) {
        return new GWTPollResult("Up", date, "Everything is A-OK", m_random.nextDouble() * 300);
    }

    private GWTPollResult down(Date date, String reason) {
        return new GWTPollResult("Down", date, reason, null);
    }

    private ApplicationInfo getApplication(String name) {
        ApplicationInfo app = m_applications.get(name);
        if (app == null) {
            Set<GWTMonitoredService> services = new TreeSet<GWTMonitoredService>();
            services.add(getMonitoredService());
            Set<String> locationNames = new TreeSet<String>();
            locationNames.add("TestApp1");
            app = new ApplicationInfo(count++, name, services, locationNames, StatusDetails.down("busted"));
            m_applications.put(name, app);
        }
        return app;
    }

    private GWTLocationSpecificStatus getStatus(Date date, String monitorName, String monitorId, GWTPollResult result) {
        GWTLocationSpecificStatus status = new GWTLocationSpecificStatus();
        status.setId(count++);
        status.setLocationMonitor(getLocationMonitor(date, monitorName, monitorId, "STARTED"));
        status.setMonitoredService(getMonitoredService());
        status.setPollResult(result);
        return status;
    }

    private GWTMonitoredService getMonitoredService() {
        return getMonitoredService("HTTP");
    }

    private GWTMonitoredService getMonitoredService(String serviceName) {
        GWTMonitoredService service = m_services.get(serviceName);
        if (service == null) {
            service = new GWTMonitoredService();
            service.setId(count++);
            Set<String> appNames = new TreeSet<String>();
            appNames.add("TestApp1");
            appNames.add("TestApp3");
            service.setApplications(appNames);
            service.setHostname("localhost");
            service.setIfIndex(count++);
            service.setIpAddress("127.0.0.1");
            service.setIpInterfaceId(count++);
            service.setNodeId(count++);
            service.setServiceName(serviceName);
            m_services.put(serviceName, service);
        }
        return service;
    }

    private GWTLocationMonitor getLocationMonitor(Date checkinTime, String name, String id, String status) {
        final String monitorName = name + "-" + id;
        GWTLocationMonitor monitor = m_monitors.get(monitorName);
        if (monitor == null) {
            monitor = new GWTLocationMonitor();
            monitor.setId(id);
            monitor.setDefinitionName(name);
            monitor.setLastCheckInTime(checkinTime);
            monitor.setName(monitorName);
            monitor.setStatus(status);
            m_monitors.put(monitorName, monitor);
        } else {
            monitor.setLastCheckInTime(checkinTime);
            monitor.setStatus(status);
        }
        return monitor;
    }
}
