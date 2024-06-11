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
        List<String> templates = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
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
