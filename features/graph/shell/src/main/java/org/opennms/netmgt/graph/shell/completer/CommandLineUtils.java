/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.shell.completer;

import java.util.List;

import org.apache.karaf.shell.api.console.CommandLine;

import com.google.common.collect.Lists;

public class CommandLineUtils {

    public static String extractArgument(final CommandLine commandLine, final String argumentName) {
        final List<String> arguments = Lists.newArrayList(commandLine.getArguments());
        return extractArgument(arguments, argumentName);
    }

    public static String extractArgument(final List<String> arguments, final String argumentName) {
        if (arguments.contains(argumentName)) {
            int index = arguments.indexOf(argumentName);
            if (arguments.size() > index) {
                return arguments.get(index + 1);
            }
            return null;
        }
        return null;
    }
}
