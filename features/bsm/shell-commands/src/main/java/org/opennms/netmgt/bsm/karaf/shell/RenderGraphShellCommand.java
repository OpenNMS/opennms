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
package org.opennms.netmgt.bsm.karaf.shell;

import java.io.File;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;

@Command(scope = "opennms", name = "bsm-render-graph", description="Renders the current state machine graph to a .png file.")
@Service
public class RenderGraphShellCommand implements Action {

    @Reference
    public BusinessServiceStateMachine businessServiceStateMachine;

    @Override
    public Object execute() throws Exception {
        File tempFile = File.createTempFile("bsm-state-machine", ".png");
        businessServiceStateMachine.renderGraphToPng(tempFile);
        System.out.println("Succesfully rendered state machine graph to " + tempFile.getAbsolutePath());
        return null;
    }
}
