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

package org.opennms.netmgt.events.commands;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.daemon.DaemonInfo;
import org.opennms.netmgt.daemon.DaemonService;

@Command(scope = "daemon", name = "list", description = "List all daemons")
@Service
public class DaemonListCommand implements Action {

    @Reference
    private DaemonService daemonService;

    @Option(name="-a", description = "Show all daemons. By default only enabled AND reloadable daemons are shown")
    private boolean showAll = false;

    @Override
    public Object execute() throws Exception {
        final List<DaemonInfo> daemons = daemonService.getDaemons()
                .stream()
                .filter(d -> !d.isInternal() && (showAll || d.isReloadable() && d.isEnabled()))
                .sorted(Comparator.comparing(DaemonInfo::getName))
                        .collect(Collectors.toList());
        final int maxDaemonName = daemons.stream().mapToInt(d -> d.getName().length()).max().getAsInt();
        final String format = String.format("%%-%ds     %%-%ds     %%s", maxDaemonName, "Enabled".length());
        System.out.println(String.format(format, "Name", "Enabled", "Reloadable"));
        daemons.forEach(d -> System.out.println(String.format(format, d.getName(), d.isEnabled(), d.isReloadable())));
        return null;
    }
}
