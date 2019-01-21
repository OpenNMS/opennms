/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.daemon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.events.EventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.collect.Sets;

public class DefaultDaemonService implements DaemonService {

    @Autowired
    private EventForwarder eventForwarder;

    @Autowired
    private EventDao eventDao;

    @Autowired
    @Qualifier("service-configuration.xml")
    private ConfigurationResource<ServiceConfiguration> serviceConfiguration;

    private final List<DaemonInfo> daemonList = new ArrayList<>();

    private final Set<String> ignoreList = Sets.newHashSet("Manager", "TestLoadLibraries");

    @PostConstruct
    public void init() throws ConfigurationResourceException {
        final ServiceConfiguration config = serviceConfiguration.get();
        for (Service service : config.getServices()) {
            final String name = service.getName().split("=")[1]; // OpenNMS:Name=Manager => Manager
            daemonList.add(new DaemonInfo(name, ignoreList.contains(name), service.isEnabled(), service.isReloadable() /* Internal daemons are not reloadable by default */));
        }
    }

    @Override
    public List<DaemonInfo> getDaemons() {
        return new ArrayList(daemonList);
    }

    @Override
    public void triggerReload(String daemonName) throws DaemonReloadException {
        final DaemonInfo dameonInfo = daemonList.stream()
                .filter(daemon -> daemon.getName().equalsIgnoreCase(daemonName))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException());

        if (!dameonInfo.isEnabled()) {
            throw new DaemonReloadException("Daemon with name " + daemonName + " is not enabled and therefore cannot be reloaded");
        }
        if (!dameonInfo.isReloadable()) {
            throw new DaemonReloadException("Daemon with name " + daemonName + " is not reloadable");
        }
        if (dameonInfo.isReloadable()) {
            EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "Daemon Config Service");
            eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, daemonName.toLowerCase());
            eventForwarder.sendNow(eventBuilder.getEvent());
        }
    }

    @Override
    public DaemonReloadInfo getCurrentReloadState(String daemonName) {
        if (!daemonList.stream().anyMatch(daemon -> daemon.getName().equalsIgnoreCase(daemonName))) {
            throw new NoSuchElementException();
        }

        // Retrieve the last (youngest) reload event of the given daemon
        final List<OnmsEvent> lastReloadEventList = eventDao.findMatching(
                new CriteriaBuilder(OnmsEvent.class)
                        .alias("eventParameters", "eventParameters")
                        .and(
                                Restrictions.eq("eventUei", EventConstants.RELOAD_DAEMON_CONFIG_UEI),
                                Restrictions.eq("eventParameters.name", "daemonName"),
                                Restrictions.ilike("eventParameters.value", daemonName)
                        )
                        .orderBy("eventTime")
                        .desc()
                        .limit(1)
                        .toCriteria()
        );

        // If no such event exists, the ReloadState is Unknown
        if (lastReloadEventList.isEmpty()) {
            return new DaemonReloadInfo(null, null, DaemonReloadState.Unknown);
        }

        // If there is a last event time, check for a SUCCESS or FAILED event
        long lastReloadEventTime = lastReloadEventList.get(0).getEventTime().getTime();
        final List<OnmsEvent> lastReloadResultEventList = eventDao.findMatching(
                new CriteriaBuilder(OnmsEvent.class)
                        .alias("eventParameters", "params")
                        .and(
                                Restrictions.or(
                                        Restrictions.eq("eventUei", EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI),
                                        Restrictions.eq("eventUei", EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI)
                                ),
                                Restrictions.ilike("params.value", daemonName),
                                Restrictions.eq("params.name", "daemonName"),
                                Restrictions.gt("eventTime", new Date(lastReloadEventTime))
                        )
                        .orderBy("eventTime")
                        .desc()
                        .limit(1)
                        .toCriteria()
        );

        // Not any events have been received yet, so daemon is still reloading
        if (lastReloadResultEventList.isEmpty()) {
            return new DaemonReloadInfo(lastReloadEventTime, null, DaemonReloadState.Reloading);
        }

        // An event has been received, now decide if Success or Failed
        final OnmsEvent lastReloadResultEvent = lastReloadResultEventList.get(0);
        if (lastReloadResultEvent.getEventUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI)) {
            return new DaemonReloadInfo(lastReloadEventTime, lastReloadResultEvent.getEventTime().getTime(), DaemonReloadState.Success);
        } else {
            return new DaemonReloadInfo(lastReloadEventTime, lastReloadResultEvent.getEventTime().getTime(), DaemonReloadState.Failed);
        }
    }
}