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
