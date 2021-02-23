/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 2021-2021 The OpenNMS Group, Inc.
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


package org.opennms.config.osgilistener;

import java.util.HashMap;
import java.util.Map;

import org.opennms.config.configservice.api.ConfigurationService;
import org.opennms.config.osgi.OsgiConfigAdaptor;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;

public class ManagedServiceListener {

    final ConfigurationService configurationService;
    final OsgiConfigAdaptor osgiConfigAdaptor;
    final ConfigurationAdmin configurationAdmin;

    public ManagedServiceListener(final ConfigurationService configurationService,
                                  final OsgiConfigAdaptor osgiConfigAdaptor,
                                  final ConfigurationAdmin configurationAdmin) {
        this.configurationService = configurationService;
        this.osgiConfigAdaptor = osgiConfigAdaptor;
        this.configurationAdmin = configurationAdmin;
    }

    public void onBind(final ManagedService service, final Map<String, String> properties) {
        final String pid = properties.get("service.pid");
        if(OsgiConfigAdaptor.PID.equals(pid)) { // TODO: Patrick: do this for all PIDs
            if(this.configurationService.getConfigurationAsMap(pid).isPresent()) {
                // trigger loading configuration from ConfigurationService
                osgiConfigAdaptor.configurationHasChanged(pid);
            } else {
                // we have no configuration for this service. Lets start with an empty one...
                // TODO: Patrick: access the blueprint default values
                this.configurationService.putConfiguration(pid, new HashMap<>()); // set an empty config
            }
        }
    }

    public void onUnbind(final ManagedService service, final Map<String, String> properties) {
        // do nothing
    }
}
