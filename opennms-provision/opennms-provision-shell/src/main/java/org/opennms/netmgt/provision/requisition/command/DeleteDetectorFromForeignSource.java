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

import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Command(scope = "opennms", name = "delete-detector-from-foreignsource", description = "Delete a node from a Provisioning Requisition.")
@Service
public class DeleteDetectorFromForeignSource implements Action {
    @Reference
    @Autowired
    @Qualifier("pending")
    private ForeignSourceRepository ForeignSourceRepository;

    @Option(name = "-f", aliases = "--foreignsource", description = "Foreign Source Name", required = true)
    @Completion(RequisitionNameCompleter.class)
    private String fsName;

    @Option(name = "-n", aliases = "--name", description = "The friendly name of the detector to delete. multiple '-n' ok.", required = true, multiValued = true)
    private List<String> detectorName;

    @Override
    public Object execute() {
        try {
            if (doesFSDExist()) {
                ForeignSource fsd = ForeignSourceRepository.getForeignSource(fsName);
                int count = 0;
                List<PluginConfig> theseDetectors = fsd.getDetectors();
                java.util.Iterator<PluginConfig> i = theseDetectors.iterator();
                while (i.hasNext()) {
                    PluginConfig pc = i.next();
                    for (String name : detectorName) {
                        if (pc.getName().equals(name)) {
                            i.remove();
                            count++;
                            System.out.println("Deleting " + name + "...");
                        }
                    }
                }
                ForeignSourceRepository.delete(fsd); // delete the fsd first
                fsd.setDetectors(theseDetectors); // add the detectors
                fsd.updateDateStamp();
                ForeignSourceRepository.save(fsd); // save the modified FSD
                ForeignSourceRepository.validate(fsd);
                System.out.println("Deleted " + count + " detector(s).");
            } else {
                System.out.println("Foreign Source '" + fsName + "' not found.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return null;
    }

    private boolean doesFSDExist() {
        boolean fsExists = true;
        ForeignSource someFS = null;
        try {
            someFS = ForeignSourceRepository.getForeignSource(fsName);
        } catch (ForeignSourceRepositoryException e) {
            fsExists = false;
        }
        if (someFS == null) {
            fsExists = false;
        }
        return fsExists;
    }
}
