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

import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.completers.StringsCompleter;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphService;

@Service
public class ContainerNamespaceCompleter implements Completer {

    @Reference
    private GraphService graphService;

    @Override
    public int complete(Session session, CommandLine commandLine, List<String> candidates) {
        // If container is already specified, try limiting the completion to the container instead of showing all
        final String containerId = CommandLineUtils.extractArgument(commandLine, "--container");
        if (containerId != null) {
            final GraphContainerInfo graphContainerInfo = graphService.getGraphContainerInfo(containerId);
            if (graphContainerInfo != null) {
                final List<String> namespaces = graphContainerInfo.getNamespaces();
                final StringsCompleter delegate = new StringsCompleter();
                delegate.getStrings().addAll(namespaces);
                return delegate.complete(session, commandLine, candidates);
            }
            return -1; // no result
        }
        return new NamespaceCompleter(graphService).complete(session, commandLine, candidates);
    }
}
