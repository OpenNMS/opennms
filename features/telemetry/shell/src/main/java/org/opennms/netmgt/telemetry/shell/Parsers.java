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
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.opennms.netmgt.telemetry.api.TelemetryManager;
import org.opennms.netmgt.telemetry.api.receiver.Listener;

import com.google.gson.JsonObject;

@Command(scope = "opennms", name = "telemetry-parsers", description = "Lists configured telemetry parsers")
@Service
public class Parsers implements Action {

    @Reference
    public TelemetryManager manager;

    @Argument(index = 0, name = "listener", description = "The listener to show parsers for", required = true, multiValued = false)
    @Completion(value = ListenerCompleter.class)
    public String listener;

    @Argument(index = 1, name = "parser", description = "Filter parsers shown by this RegEx", required = false)
    public String parserFilter = ".*";

    @Option(name = "-s", aliases = "--state", description = "Show internal state", required = false, multiValued = false)
    public boolean showState = false;

    @Option(name = "-f", aliases = "--format", description = "Dump data in given format", required = false, multiValued = false)
    @Completion(value = StringsCompleter.class, values = { "PLAIN", "JSON" })
    public Format format = Format.PLAIN;

    @Override
    public Object execute() {
        final Optional<Listener> listener = this.manager.getListeners().stream()
                                                        .filter(l -> Objects.equals(l.getName(), this.listener))
                                                        .findAny();

        if (listener.isPresent()) {
            final List<JsonObject> output = listener
                    .get().getParsers().stream()
                    .filter(parser -> parser.getName().matches(this.parserFilter))
                    .map(parser -> {
                        final JsonObject data = new JsonObject();
                        data.addProperty("name", parser.getName());
                        data.addProperty("description", parser.getDescription());

                        data.add("properties", Utils.getWritableProperties(parser));

                        if (this.showState) {
                            data.add("state", Utils.GSON.toJsonTree(parser.dumpInternalState()));
                        }

                        return data;
                    }).collect(Collectors.toList());

            this.format.print(output);

        } else {
            System.err.printf("No such listener: %s\n", this.listener);
        }

        return null;
    }

    @Service
    public static class ListenerCompleter implements Completer {

        @Reference
        public TelemetryManager manager;

        @Override
        public int complete(final Session session, final CommandLine commandLine, final List<String> candidates) {
            final StringsCompleter delegate = new StringsCompleter(this.manager.getListeners().stream()
                                                                               .map(Listener::getName)
                                                                               .collect(Collectors.toList()));
            return delegate.complete(session, commandLine, candidates);
        }
    }
}
