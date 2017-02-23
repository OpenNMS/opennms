/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.mibcompiler.services;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.model.PrefabGraph;

/**
 * The Class PrefabGraphDumper.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class PrefabGraphDumper {

    /**
     * Dump.
     * <p>This only cover the main variables from the PrefabGraph.</p>
     *
     * @param graphs the graphs
     * @param writer the writer
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void dump(List<PrefabGraph> graphs, Writer writer) throws IOException {
        List<String> templates = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        for (PrefabGraph graph : graphs) {
            String name = "report." + graph.getName();
            templates.add(graph.getName());
            sb.append(name).append(".name=").append(graph.getTitle()).append("\n");
            sb.append(name).append(".columns=").append(StringUtils.join(graph.getColumns(), ",")).append("\n");
            sb.append(name).append(".type=").append(StringUtils.join(graph.getTypes(), ",")).append("\n");
            sb.append(name).append(".description=").append(graph.getDescription()).append("\n");
            sb.append(name).append(".command=").append(graph.getCommand());
        }
        writer.write("reports=" + StringUtils.join(templates, ", \\\n") + "\n\n");
        writer.write(sb.toString());
    }

}
