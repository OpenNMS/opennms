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
