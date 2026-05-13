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
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;

@Command(scope = "opennms", name = "delete-foreignsource", description = "Delete a named foreign source definition.")
@Service
public class DeleteForeignSource implements Action {
    @Reference
    private ForeignSourceRepository deployedForeignSourceRepository;

    @Option(name = "-f", aliases = "--foreignsource", description = "Foreign Source Name", required = true)
    @Completion(RequisitionNameCompleter.class)
    private String fsName;

    @Override
    public Object execute() {
        try {
            if (RequisitionCmdCommon.doesFSDExist(deployedForeignSourceRepository, fsName)) {
                ForeignSource fsd = deployedForeignSourceRepository.getForeignSource(fsName);
                deployedForeignSourceRepository.delete(fsd);
                System.out.println("Deleted foreign source '" + fsName + "'.");
            } else {
                System.out.println("Foreign Source '" + fsName + "' not found.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}