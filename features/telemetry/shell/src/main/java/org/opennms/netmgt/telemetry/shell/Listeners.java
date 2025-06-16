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
package org.opennms.netmgt.telemetry.shell;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.opennms.netmgt.telemetry.api.TelemetryManager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@Command(scope = "opennms", name = "telemetry-listeners", description = "Lists configured telemetry listeners")
@Service
public class Listeners implements Action {

    @Reference
    public TelemetryManager manager;

    @Argument(index = 0, name = "listener", description = "Filter listeners shown by this RegEx", required = false)
    public String listenerFilter = ".*";

    @Option(name = "-f", aliases = "--format", description = "Dump data in given format", required = false, multiValued = false)
    @Completion(value = StringsCompleter.class, values = {"PLAIN", "JSON" })
    public Format format = Format.PLAIN;

    @Override
    public Object execute() {
        final List<JsonObject> output = this.manager
                .getListeners().stream()
                .filter(listener -> listener.getName().matches(this.listenerFilter))
                .map(listener -> {
                    final JsonObject data = new JsonObject();
                    data.addProperty("name", listener.getName());
                    data.addProperty("description", listener.getDescription());

                    data.add("properties", Utils.getWritableProperties(listener));

                    final var parsers = new JsonArray();
                    listener.getParsers().forEach(parser -> parsers.add(parser.getName()));
                    data.add("parsers", parsers);

                    return data;
                }).collect(Collectors.toList());

        this.format.print(output);

        return null;
    }
}
