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
package org.opennms.core.ipc.twin.shell;

import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TwinKeyCompleter implements Completer {

    public static final Map<String, String> TWIN_KEY_CLASS_MAP = new LinkedHashMap<>();

    static {
        TWIN_KEY_CLASS_MAP.put("trapd.listener.config", "org.opennms.netmgt.snmp.TrapListenerConfig");
        TWIN_KEY_CLASS_MAP.put("ipfix-dot-d.config", "org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.IpfixDotD");
    }

    @Override
    public int complete(Session session, CommandLine commandLine, List<String> list) {
        StringsCompleter keys = new StringsCompleter();
        keys.getStrings().addAll(TWIN_KEY_CLASS_MAP.keySet());
        return keys.complete(session, commandLine, list);
    }
}
