/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.deviceconfig.tftp.TftpServer;

@Command(scope = "device-config", name = "tftp-server-statistics", description = "View or reset TFTP server statistics")
@Service
public class TftpServerStatisticsCommand implements Action {

    @Reference
    protected TftpServer tftpServer;

    @Option(name = "-a", aliases = "--action", description = "Determines if TFTP server statistics are viewed or reset", valueToShowInHelp = "view")
    private CommandAction action = CommandAction.view;

    @Override
    public Object execute() {
        var s = action == CommandAction.view ? tftpServer.getStatistics() : tftpServer.getAndResetStatistics();
        System.out.println("Files received: " + s.filesReceived());
        System.out.println("Bytes received: " + s.bytesReceived());
        if (s.warnings() != 0) {
            // output number of errors in red if there are errors
            System.out.println("\033[0;31mWarnings      : " + s.warnings() + "\033[0m");
        } else {
            System.out.println("Warnings      : " + s.warnings());
        }
        if (s.errors() != 0) {
            // output number of errors in red if there are errors
            System.out.println("\033[0;31mErrors        : " + s.errors() + "\033[0m");
        } else {
            System.out.println("Errors        : " + s.errors());
        }
        return null;
    }

    enum CommandAction {
        view, reset;
    }
}
