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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;

import java.util.List;


@Command(scope = "opennms", name = "add-detector-to-fsd", description = "Add a detector to a named foreign source definition. If the foreign Source definition doesn't exist, it will be created.")
@Service
public class AddDetectorToForeignSource implements Action {

    @Reference
    private ForeignSourceRepository deployedForeignSourceRepository;

    @Option(name = "-f", aliases = "--foreignsource", description = "The foreign source name to which this detector should be added, or the name of a new foreign source to create.", required = true)
    @Completion(RequisitionNameCompleter.class)
    private String fsName;
    @Option(name = "-n", aliases = "--name", description = "The service name for the detector.", required = true)
    private String detectorName;
    @Option(name = "-c", aliases = "--class", description = "The detector class.  Tab for autocomplete.", required = true)
    @Completion(DetectorClassCompleter.class)
    String detectorClass = null;
    @Option(name = "-p", aliases = "--parameters", description = "Detector parameters, multiple '-p' ok, 'key=value' pairs, e.g. \"-p port=8080 -p timeout=3000\"", multiValued = true)
    List<String> parameters = null;
    @Option(name = "-v", aliases = "--verbose", description = "Be verbose; show us the detector XML")
    private boolean verbose = false;

    public Object execute() {
        // check if the requisition exists
        ForeignSource theFSD;
        try {
            if (doesFSDExist()) {
                theFSD = deployedForeignSourceRepository.getForeignSource(fsName);
            } else {
                // if not create it
                theFSD = new ForeignSource(fsName);
                System.out.println("Creating foreign source definition '" + fsName + "'.");
            }

            // create the detector
            PluginConfig theDetector = new PluginConfig(detectorName, detectorClass);

            // do params
            if (parameters != null) {
                for (String thisKVpair : parameters) {
                    String[] keyValue = thisKVpair.split("=");
                    theDetector.addParameter(keyValue[0], keyValue[1]);
                }
            }
            if (verbose) {
                System.out.println();
                System.out.println(JaxbUtils.marshal(theDetector));
                System.out.println();
            }
            theFSD.addDetector(theDetector);
            deployedForeignSourceRepository.save(theFSD);
        }
        catch(Exception e) {
            System.out.println("Failed to add \"" + detectorName + "\" to \""+ fsName +"\"!");
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            return null;
        }
        System.out.println("Successfully added detector \"" + detectorName + "\" to \""+ fsName +"\"!");
        return null;
    }

    private boolean doesFSDExist() {
        boolean fsExists = true;
        ForeignSource someFS = null;
        try {
            someFS = deployedForeignSourceRepository.getForeignSource(fsName);
        } catch (ForeignSourceRepositoryException e) {
            fsExists = false;
        }
        if (someFS == null) {
            fsExists = false;
        }
        return fsExists;
    }
}