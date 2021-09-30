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
