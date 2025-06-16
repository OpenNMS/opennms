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
package org.opennms.netmgt.events.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;

import com.google.common.base.Strings;

@Command(scope = "opennms", name = "reload-daemon", description = "Reload a specific daemon")
@Service
public class ReloadDaemonCommand implements Action {

    @Reference
    public EventForwarder eventForwarder;
    
    @Option(name = "-f", aliases = "--config-file", description = "Optional config-file to target for reload", required = false, multiValued = false)
    private String configFile;

    @Argument(index = 0, name = "daemonName", description = "deamon to reload", required = true, multiValued = false)
    @Completion(DaemonNameCompleter.class)
    String daemonName;

    @Override
    public Object execute() throws Exception {

        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "reload-daemon-command");
        eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, daemonName);
        if (! Strings.isNullOrEmpty(configFile)) {
            eventBuilder.addParam(EventConstants.PARM_CONFIG_FILE_NAME, configFile);
        }
        eventForwarder.sendNow(eventBuilder.getEvent());
        return null;
    }
}
