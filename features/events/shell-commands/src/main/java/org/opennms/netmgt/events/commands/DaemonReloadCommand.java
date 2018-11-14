/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

import java.util.NoSuchElementException;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.daemon.DaemonReloadInfo;
import org.opennms.netmgt.daemon.DaemonReloadState;
import org.opennms.netmgt.daemon.DaemonService;

@Command(scope = "daemon", name = "reload", description = "Reload a specific daemon")
@Service
public class DaemonReloadCommand implements Action {

    @Reference
    private DaemonService daemonService;

    @Option(name="-b", description="block and wait for reload state")
    private boolean blockAndWait = false;

    @Argument(index = 0, name = "daemonName", description = "Name of the daemon to reload", required = true, multiValued = false)
    @Completion(DaemonNameCompleter.class)
    String daemonName;

    @Override
    public Object execute() throws Exception {
        try {
            if (blockAndWait) {
                final long started = System.currentTimeMillis();
                System.out.print("Reloading daemon '" + daemonName +"' ...");
                daemonService.triggerReload(daemonName);
                for (int i=0; i < 5001; i++) {
                    Thread.sleep(500);
                    final DaemonReloadInfo currentReloadState = daemonService.getCurrentReloadState(daemonName);
                    if (currentReloadState.getReloadRequestEventTime() >= started
                            && currentReloadState.getReloadState() != DaemonReloadState.Reloading
                            && currentReloadState.getReloadState() != DaemonReloadState.Unknown) {
                        System.out.println();
                        if (currentReloadState.getReloadState() == DaemonReloadState.Success) {
                            System.out.println("Daemon reloaded successfully.");
                        } else {
                            System.out.println("Daemon reloading failed.");
                        }
                        return null;
                    }
                    System.out.print(".");
                }
                System.out.println();
                System.out.println("Daemon reload state is unclear");
            } else {
                daemonService.triggerReload(daemonName);
            }
        } catch (NoSuchElementException e) {
            System.err.println("Daemon with name '" + daemonName + "' is unknown");
        }

        return null;
    }
}
