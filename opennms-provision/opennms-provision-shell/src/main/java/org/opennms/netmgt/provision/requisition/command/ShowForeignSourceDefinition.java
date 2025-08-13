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

package org.opennms.netmgt.provision.requisition.command;

import org.apache.karaf.shell.api.action.*;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.foreignsource.PluginParameter;

import java.util.Set;

@Command(scope = "opennms", name = "show-foreignsource-definition", description = "Display defined Foreign Source Definitions.")
@Service
public class ShowForeignSourceDefinition implements Action {
    @Reference
    private ForeignSourceRepository foreignSource;

    @Option(name = "-x", aliases = "--xml", description = "Show foreign source as XML")
    private boolean asXml = false;

    @Argument(index = 0, name = "foreignSourceName", description = "Foreign Source name")
    @Completion(RequisitionNameCompleter.class)
    private String foreignSourceName = null;

    @Override
    public Object execute() {
        try {
            if (asXml && foreignSourceName == null) {
                System.out.println(" -x (--xml) requires a Foreign Source Name -f (--foreignsource)");
                return null;
            }
            if (foreignSourceName != null) {
                final ForeignSource fsd = foreignSource.getForeignSource(foreignSourceName);
                if (asXml) {
                    System.out.println();
                    System.out.println(JaxbUtils.marshal(fsd));
                    System.out.println();
                    return null;
                } else {
                    ShellTable table = new ShellTable();
                    table.column("Name");
                    table.column("Class");
                    table.column("Parameters");
                    for (PluginConfig pc : fsd.getDetectors()) {
                        String parameters = "";
                        for (PluginParameter pl : pc.getParameters()) {
                            parameters += pl.getKey() + "=" + pl.getValue()+"\n";
                        }
                        table.addRow().addContent(pc.getName(), pc.getPluginClass(), parameters);
                    }
                    for (PluginConfig pc : fsd.getPolicies()) {
                        String parameters = "";
                        for (PluginParameter pl : pc.getParameters()) {
                            parameters += pl.getKey() + "=" + pl.getValue() + "\n";
                        }
                        table.addRow().addContent(pc.getName(), pc.getPluginClass(), parameters);
                    }
                    table.print(System.out);
                }
            }
            else { // no options, show a table of general FSD info
                final Set<ForeignSource> allForeignSources = foreignSource.getForeignSources();
                ShellTable table = new ShellTable();
                table.column("Name");
                table.column("Datestamp");
                table.column("Detectors");
                table.column("Policies");
                for (ForeignSource fs : allForeignSources) {
                    table.addRow().addContent(fs.getName(), fs.getDateStampAsDate(), fs.getDetectors().size(), fs.getPolicies().size());
                }
                table.print(System.out);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }

        return null;
    }
}